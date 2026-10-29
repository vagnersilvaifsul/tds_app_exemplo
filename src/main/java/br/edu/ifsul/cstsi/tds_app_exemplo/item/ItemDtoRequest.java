package br.edu.ifsul.cstsi.tds_app_exemplo.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/*
    DTO de entrada de um item do pedido: só o produto e a quantidade.
    O valor do item NÃO vem do cliente da API: é calculado no servidor a partir do valor de venda do produto
    (nunca confie em preços enviados pelo app).
 */
public record ItemDtoRequest(
        @NotNull(message = "O produto é obrigatório")
        Long produtoId,
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade
) {
}
