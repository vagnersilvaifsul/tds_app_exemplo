package br.edu.ifsul.cstsi.tds_app_exemplo.produto;

import java.math.BigDecimal;


public record ProdutoDtoPost(
    String nome,
    String descricao,
    BigDecimal valorDeCompra,
    BigDecimal valorDeVenda,
    Integer estoque
) {
    public ProdutoDtoPost(Produto produto){
        this(produto.getNome(), produto.getDescricao(), produto.getValorDeCompra(), produto.getValorDeVenda(), produto.getEstoque());
    }
}
