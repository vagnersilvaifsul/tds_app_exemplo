package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import br.edu.ifsul.cstsi.tds_app_exemplo.infra.exception.RegraDeNegocioException;
import br.edu.ifsul.cstsi.tds_app_exemplo.item.Item;
import br.edu.ifsul.cstsi.tds_app_exemplo.produto.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/*
    TRANSAÇÃO
    Uma transação é um conjunto de operações no banco de dados que deve acontecer por inteiro ou não acontecer:
    "tudo ou nada". Ela garante as propriedades ACID (Atomicidade, Consistência, Isolamento e Durabilidade).

    Exemplo deste service: criar um pedido envolve várias operações
      1. gravar o pedido;
      2. gravar cada item;
      3. baixar o estoque de cada produto.
    Se o 3º item não tiver estoque suficiente, os passos já executados para os itens 1 e 2 NÃO podem ficar gravados.
    Sem transação, o banco ficaria inconsistente (estoque baixado de um pedido que não existe).

    @Transactional (Spring)
      - Antes do método começar, o Spring abre uma transação; se o método terminar normalmente, faz o COMMIT
        (grava tudo); se lançar uma RuntimeException, faz o ROLLBACK (desfaz tudo).
      - Dentro da transação, as entidades buscadas ficam "gerenciadas" pelo JPA: alterar um atributo (ex.:
        produto.setEstoque(...)) já basta para o UPDATE acontecer no commit, sem chamar save() (dirty checking).
      - É também dentro da transação que as associações LAZY (ex.: pedido.getItems()) podem ser carregadas.
      - readOnly = true (usado na classe, como padrão): avisa que o método só lê; o Hibernate otimiza a consulta.
        Os métodos que escrevem sobrescrevem com @Transactional (readOnly = false é o padrão).
      - Atenção: a anotação só funciona em métodos públicos chamados a partir de outro bean (ex.: o controller).
        Uma chamada de um método para outro dentro da mesma classe não passa pelo "proxy" do Spring e ignora a anotação.
 */
@Service
@Transactional(readOnly = true)
public class PedidoService {

    //estados em que o pedido ainda pode ser cancelado (depois de enviado, não pode mais)
    private static final EnumSet<EstadoPedido> CANCELAVEIS = EnumSet.of(EstadoPedido.NOVO, EstadoPedido.PAGO, EstadoPedido.EM_SEPARACAO);

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<PedidoDtoResponse> findAll() {
        return pedidoRepository.findAll().stream().map(PedidoDtoResponse::new).toList();
    }

    public List<PedidoDtoResponse> findByCliente(Cliente cliente) {
        return pedidoRepository.findByClienteIdOrderByIdDesc(cliente.getId()).stream().map(PedidoDtoResponse::new).toList();
    }

    public Optional<PedidoDtoResponse> findById(Long id, Cliente usuarioLogado) {
        return pedidoRepository.findById(id).map(pedido -> {
            verificaDonoOuAdmin(pedido, usuarioLogado);
            return new PedidoDtoResponse(pedido);
        });
    }

    @Transactional //escreve no banco: pedido, itens e estoque (tudo ou nada)
    public PedidoDtoResponse insert(PedidoDtoRequest dto, Cliente cliente) {
        var pedido = new Pedido();
        pedido.setPagamento(dto.pagamento());
        pedido.setEstado(EstadoPedido.NOVO);
        pedido.setSituacao(Boolean.TRUE);
        pedido.setDataCriacao(LocalDate.now());
        pedido.setDataModificacao(LocalDate.now());
        pedido.setCliente(cliente);

        var total = BigDecimal.ZERO;
        for (var itemDto : dto.itens()) {
            var produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDto.produtoId()));
            if (!Boolean.TRUE.equals(produto.getSituacao())) {
                throw new RegraDeNegocioException("O produto " + produto.getNome() + " não está disponível para venda");
            }
            if (produto.getEstoque() < itemDto.quantidade()) {
                //RuntimeException: o Spring faz o ROLLBACK e nada do que foi feito nas voltas anteriores do laço fica gravado
                throw new RegraDeNegocioException("Estoque insuficiente para o produto " + produto.getNome()
                        + " (disponível: " + produto.getEstoque() + ", solicitado: " + itemDto.quantidade() + ")");
            }
            produto.setEstoque(produto.getEstoque() - itemDto.quantidade()); //dirty checking: o UPDATE vai no commit

            var item = new Item();
            item.setProduto(produto);
            item.setQuantidade(itemDto.quantidade());
            item.setTotalItem(produto.getValorDeVenda().multiply(BigDecimal.valueOf(itemDto.quantidade())));
            item.setSituacao(Boolean.TRUE);
            item.setPedido(pedido); //o lado "dono" da associação é o Item (ele tem a chave estrangeira pedido_id)
            pedido.getItems().add(item);
            total = total.add(item.getTotalItem());
        }
        pedido.setTotalPedido(total);

        return new PedidoDtoResponse(pedidoRepository.save(pedido)); //cascade PERSIST: salva os itens junto
    }

    @Transactional
    public Optional<PedidoDtoResponse> alteraEstado(Long id, EstadoPedido novoEstado) {
        return pedidoRepository.findById(id).map(pedido -> {
            if (!Boolean.TRUE.equals(pedido.getSituacao())) {
                throw new RegraDeNegocioException("O pedido " + id + " está cancelado e não pode mudar de estado");
            }
            if (novoEstado.ordinal() <= pedido.getEstado().ordinal()) {
                throw new RegraDeNegocioException("O pedido só avança de fase: está em " + pedido.getEstado()
                        + " e não pode ir para " + novoEstado);
            }
            pedido.setEstado(novoEstado);
            pedido.setDataModificacao(LocalDate.now());
            return new PedidoDtoResponse(pedido);
        });
    }

    /*
        Cancelamento (o "delete" do pedido): o pedido não é apagado, ele fica com situacao = false e o estoque dos
        produtos é devolvido. Também é tudo ou nada: ou o pedido é cancelado e todo o estoque volta, ou nada muda.
     */
    @Transactional
    public Optional<PedidoDtoResponse> cancela(Long id, Cliente usuarioLogado) {
        return pedidoRepository.findById(id).map(pedido -> {
            verificaDonoOuAdmin(pedido, usuarioLogado);
            if (!Boolean.TRUE.equals(pedido.getSituacao())) {
                throw new RegraDeNegocioException("O pedido " + id + " já está cancelado");
            }
            if (!CANCELAVEIS.contains(pedido.getEstado())) {
                throw new RegraDeNegocioException("O pedido " + id + " já foi " + pedido.getEstado() + " e não pode ser cancelado");
            }
            for (var item : pedido.getItems()) {
                var produto = item.getProduto();
                produto.setEstoque(produto.getEstoque() + item.getQuantidade()); //devolve o estoque
                item.setSituacao(Boolean.FALSE);
            }
            pedido.setSituacao(Boolean.FALSE);
            pedido.setDataModificacao(LocalDate.now());
            return new PedidoDtoResponse(pedido);
        });
    }

    //regra de autorização que depende do dado (o dono do pedido), por isso não dá para resolver só com @Secured
    private void verificaDonoOuAdmin(Pedido pedido, Cliente usuarioLogado) {
        boolean admin = usuarioLogado.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin && !pedido.getCliente().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("O pedido " + pedido.getId() + " pertence a outro cliente");
        }
    }
}
