package br.edu.ifsul.cstsi.tds_app_exemplo.produto;

import java.math.BigDecimal;

/*
    DTO (Data Transfer Object) de SAÍDA: é o que a API devolve ao cliente em todas as respostas de produto.
    Repare que o valorDeCompra não sai da API: é um dado interno da loja (o cliente só precisa do valor de venda).
    Esse é um dos motivos para nunca devolver a entidade diretamente no controller.
 */
public record ProdutoDtoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorDeVenda,
        Integer estoque,
        Boolean situacao
) {
    public ProdutoDtoResponse(Produto produto) {
        this(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getValorDeVenda(),
                produto.getEstoque(), produto.getSituacao());
    }
}
