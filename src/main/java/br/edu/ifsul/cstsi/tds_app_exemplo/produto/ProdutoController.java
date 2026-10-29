package br.edu.ifsul.cstsi.tds_app_exemplo.produto;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/*
    Pelo Princípio da Responsabilidade Única (SRP - Single-responsibility Principle) os controller manipulam
    apenas DTOs.
    Porém, se a meta for entregar o produto no Curto Prazo, é aceitável não utilizar DTO no controller.
    Mas, se a meta for de Longo Prazo, a entrega de um produto em produção, é importante aplicar o SRP, e fazer
    com que a controller trabalhe apenas com DTOs, isso facilitará a manutenção no futuro e ajuda na segurança do
    aplicativo (não expõe todos os dados aos clientes do aplicativo).

    Padrão adotado neste projeto: um DTO de entrada (ProdutoDtoRequest, para POST e PUT) e um DTO de saída
    (ProdutoDtoResponse, para todas as respostas).

    Como não há regra de negócio nem transação envolvendo mais de uma operação, o controller acessa a repository
    diretamente (sem camada de serviço).
 */
@RestController//indica que esta classe é um controller REST
@RequestMapping("api/v1/produtos") //Endpoint para toda classe
public class ProdutoController {

    private final ProdutoRepository produtoRepository;

    //Realiza a injeção de dependência
    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity<List<ProdutoDtoResponse>> findAll(){
        return ResponseEntity.ok(produtoRepository.findAll().stream().map(ProdutoDtoResponse::new).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDtoResponse> findById(@PathVariable Long id){
        return produtoRepository.findById(id)
                .map(produto -> ResponseEntity.ok(new ProdutoDtoResponse(produto))) //200 OK
                .orElseGet(() -> ResponseEntity.notFound().build()); //404 Not Found
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<ProdutoDtoResponse>> findByNome(@PathVariable String nome){
        var produtos = produtoRepository.findByNomeStartingWith(nome).stream().map(ProdutoDtoResponse::new).toList();
        return produtos.isEmpty()
                ? ResponseEntity.noContent().build() //204 No Content
                : ResponseEntity.ok(produtos); //200 OK
    }

    @PostMapping
    public ResponseEntity<ProdutoDtoResponse> insert(@Valid @RequestBody ProdutoDtoRequest produtoDtoRequest, UriComponentsBuilder uriBuilder){
        var p = produtoRepository.save(new Produto(
                null,
                produtoDtoRequest.nome(),
                produtoDtoRequest.valorDeCompra(),
                produtoDtoRequest.valorDeVenda(),
                produtoDtoRequest.descricao(),
                produtoDtoRequest.situacao() == null ? Boolean.TRUE : produtoDtoRequest.situacao(), //nasce ativo, se não informado
                produtoDtoRequest.estoque()
        ));
        //o padrão REST pede que o 201 Created informe, no header Location, onde o novo recurso pode ser consultado
        var location = uriBuilder.path("api/v1/produtos/{id}").buildAndExpand(p.getId()).toUri();
        return ResponseEntity.created(location).body(new ProdutoDtoResponse(p)); //201 Created
    }

    /*
        Antes de alterar, o produto precisa existir. Sem essa verificação, o save() com um id inexistente
        criaria um produto novo e o 404 nunca seria devolvido.
     */
    @PutMapping("{id}")
    public ResponseEntity<ProdutoDtoResponse> update(@PathVariable Long id, @Valid @RequestBody ProdutoDtoRequest produtoDtoRequest){
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(produtoDtoRequest.nome());
                    produto.setDescricao(produtoDtoRequest.descricao());
                    produto.setValorDeCompra(produtoDtoRequest.valorDeCompra());
                    produto.setValorDeVenda(produtoDtoRequest.valorDeVenda());
                    produto.setEstoque(produtoDtoRequest.estoque());
                    if (produtoDtoRequest.situacao() != null) {
                        produto.setSituacao(produtoDtoRequest.situacao()); //se não informada, mantém a atual
                    }
                    return ResponseEntity.ok(new ProdutoDtoResponse(produtoRepository.save(produto))); //200 OK
                })
                .orElseGet(() -> ResponseEntity.notFound().build()); //404 Not Found
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if (produtoRepository.existsById(id)) {
            produtoRepository.deleteById(id);
            return ResponseEntity.ok().build(); //200 OK
        }
        return ResponseEntity.notFound().build(); //404 Not Found
    }
}
