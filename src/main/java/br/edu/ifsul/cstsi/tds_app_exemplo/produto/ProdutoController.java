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
    public ResponseEntity<Produto> findById(@PathVariable Long id){
        var optionalProduto = produtoRepository.findById(id);
        return optionalProduto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Produto>> findByNome(@PathVariable String nome){
        var produtos = produtoRepository.findByNomeStartingWith(nome);
        if (produtos.isPresent() && !produtos.get().isEmpty()) {
            return ResponseEntity.ok(produtos.get());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<String> insert(@Valid @RequestBody ProdutoDtoPost produtoDTOPost, UriComponentsBuilder uriBuilder){
        var p = produtoRepository.save(new Produto(
                null,
                produtoDTOPost.nome(),
                produtoDTOPost.valorDeCompra(),
                produtoDTOPost.valorDeVenda(),
                produtoDTOPost.descricao(),
                true,
                produtoDTOPost.estoque()
        ));
        var location = uriBuilder.path("api/v1/produtos/{id}").buildAndExpand(p.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("{id}")
    public ResponseEntity<ProdutoDtoResponse> update(@PathVariable Long id, @Valid @RequestBody ProdutoDtoPut produtoDTOPut){
        var p = produtoRepository.save(new Produto(
                id,
                produtoDTOPut.nome(),
                produtoDTOPut.valorDeCompra(),
                produtoDTOPut.valorDeVenda(),
                produtoDTOPut.descricao(),
                produtoDTOPut.situacao(),
                produtoDTOPut.estoque()
        ));
        return p != null ?
                ResponseEntity.ok(new ProdutoDtoResponse(p)) :
                ResponseEntity.notFound().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        if (produtoRepository.existsById(id)) {
            produtoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
