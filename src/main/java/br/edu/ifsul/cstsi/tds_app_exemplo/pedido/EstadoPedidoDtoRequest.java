package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import jakarta.validation.constraints.NotNull;

//DTO de entrada para avançar o pedido para outra fase (ex.: { "estado": "PAGO" })
public record EstadoPedidoDtoRequest(
        @NotNull(message = "O estado é obrigatório (NOVO, PAGO, EM_SEPARACAO, ENVIADO ou ENTREGUE)")
        EstadoPedido estado
) {
}
