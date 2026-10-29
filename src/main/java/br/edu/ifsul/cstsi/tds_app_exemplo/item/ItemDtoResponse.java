package br.edu.ifsul.cstsi.tds_app_exemplo.item;

import java.math.BigDecimal;

public record ItemDtoResponse(
        Long id,
        Long produtoId,
        String produto,
        Integer quantidade,
        BigDecimal totalItem,
        Boolean situacao
) {
    public ItemDtoResponse(Item item) {
        this(item.getId(), item.getProduto().getId(), item.getProduto().getNome(), item.getQuantidade(),
                item.getTotalItem(), item.getSituacao());
    }
}
