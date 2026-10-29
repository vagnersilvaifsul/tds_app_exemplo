package br.edu.ifsul.cstsi.tds_app_exemplo.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/*
    DTO (Data Transfer Object) de ENTRADA: é o que o cliente da API envia no corpo das requisições POST e PUT.
    Note que não há id: no POST quem gera o id é o banco, e no PUT o id vem na URL (api/v1/produtos/{id}).

    A situação é opcional: no POST, se não vier, o produto nasce ativo (true); no PUT, se não vier, mantém a atual.
 */
public record ProdutoDtoRequest(
        @NotBlank(message = "O nome não pode ser nulo ou vazio")
        @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
        String nome,
        @NotBlank(message = "A descrição não pode ser nula ou vazia")
        @Size(min = 2, max = 255, message = "A descrição deve ter entre 2 e 255 caracteres")
        String descricao,
        @NotNull(message = "O valor de compra é obrigatório")
        @PositiveOrZero(message = "O valor de compra não pode ser negativo")
        BigDecimal valorDeCompra,
        @NotNull(message = "O valor de venda é obrigatório")
        @PositiveOrZero(message = "O valor de venda não pode ser negativo")
        BigDecimal valorDeVenda,
        @NotNull(message = "O estoque é obrigatório")
        @PositiveOrZero(message = "O estoque não pode ser negativo")
        Integer estoque,
        Boolean situacao
) {
}
