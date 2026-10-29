package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import br.edu.ifsul.cstsi.tds_app_exemplo.item.ItemDtoResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoDtoResponse(
        Long id,
        String pagamento,
        EstadoPedido estado,
        Boolean situacao,
        LocalDate dataCriacao,
        LocalDate dataModificacao,
        BigDecimal totalPedido,
        Long clienteId,
        String cliente,
        List<ItemDtoResponse> itens
) {
    //atenção: acessa pedido.getItems() (LAZY), por isso deve ser chamado dentro de uma transação (ver PedidoService)
    public PedidoDtoResponse(Pedido pedido) {
        this(pedido.getId(), pedido.getPagamento(), pedido.getEstado(), pedido.getSituacao(), pedido.getDataCriacao(),
                pedido.getDataModificacao(), pedido.getTotalPedido(), pedido.getCliente().getId(),
                pedido.getCliente().getNome() + " " + pedido.getCliente().getSobrenome(),
                pedido.getItems().stream().map(ItemDtoResponse::new).toList());
    }
}
