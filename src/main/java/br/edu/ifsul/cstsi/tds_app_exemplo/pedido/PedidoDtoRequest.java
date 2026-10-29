package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import br.edu.ifsul.cstsi.tds_app_exemplo.item.ItemDtoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/*
    DTO de entrada para criar um pedido. O cliente do pedido é o usuário autenticado (vem do token JWT),
    por isso não há clienteId aqui. O @Valid na lista faz a validação "descer" para cada ItemDtoRequest.
 */
public record PedidoDtoRequest(
        @NotBlank(message = "A forma de pagamento é obrigatória")
        String pagamento,
        @NotEmpty(message = "O pedido deve ter ao menos um item")
        List<@Valid ItemDtoRequest> itens
) {
}
