package br.edu.ifsul.cstsi.tds_app_exemplo.infra.mcp;

import br.edu.ifsul.cstsi.tds_app_exemplo.produto.ProdutoDtoResponse;
import br.edu.ifsul.cstsi.tds_app_exemplo.produto.ProdutoRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/*
    MCP: expõe o domínio Produto (já construído nas semanas anteriores) como
    ferramentas MCP, para que um cliente de IA (Claude Desktop, Claude Code) consiga consultá-lo em
    linguagem natural.

    Repare que nenhuma anotação nova foi necessária no ProdutoRepository e nada mudou no
    ProdutoController — o Spring AI lê a assinatura de cada método e a description de cada
    @McpToolParam para gerar o JSON Schema sozinho. É essa descrição (não o nome do parâmetro) que o
    modelo usa para decidir quando e como chamar cada ferramenta — trate-a como documentação para o
    modelo, não só para humanos.

    Ponto de atenção de segurança (discutir em aula): esta classe chama o ProdutoRepository diretamente,
    não o ProdutoController. Por isso, a anotação @Secured({"ROLE_ADMIN"}) que protege
    ProdutoController#findAll() NÃO se aplica aqui — são caminhos de código diferentes. Segurança em
    nível de método não "vaza" automaticamente para quem chama o repository por outro caminho.

    As ferramentas devolvem o mesmo DTO de saída do controller (ProdutoDtoResponse), e não a entidade Produto:
    o modelo de IA recebe só o que o cliente da API receberia. Em especial, o valorDeCompra (dado interno da loja)
    não é exposto. O DTO também desacopla o JSON Schema da ferramenta das mudanças na entidade.
 */
@Service
public class ProdutoMcpTools {

    private final ProdutoRepository produtoRepository;

    public ProdutoMcpTools(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @McpTool(description = "Lista todos os produtos cadastrados, com id, nome, descrição, preço de venda, estoque atual e situação (true = ativo, à venda)")
    public List<ProdutoDtoResponse> listarProdutos() {
        return produtoRepository.findAll().stream().map(ProdutoDtoResponse::new).toList();
    }

    @McpTool(description = "Busca produtos cujo nome começa com o texto informado")
    public List<ProdutoDtoResponse> buscarProdutoPorNome(
            @McpToolParam(description = "Início do nome do produto, ex: 'caf' para Café", required = true) String nome) {
        return produtoRepository.findByNomeStartingWith(nome).stream().map(ProdutoDtoResponse::new).toList();
    }
}
