package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/*
    Pedidos (extra). Todas as operações passam pelo PedidoService, porque envolvem transação (pedido + itens + estoque)
    ou o carregamento dos itens (LAZY) dentro de uma transação.

    @AuthenticationPrincipal injeta o cliente dono do token JWT (montado pelo CustomJwtAuthenticationConverter):
    é assim que a API sabe de quem é o pedido, sem receber um clienteId no corpo da requisição.

    Não há PUT nem DELETE: um pedido não é editado nem apagado. Ele avança de estado (PATCH /estado) ou é
    cancelado (PATCH /cancelamento), mantendo o histórico.
 */
@RestController
@RequestMapping("api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Secured({"ROLE_ADMIN"}) //todos os pedidos: só o ADMIN
    public ResponseEntity<List<PedidoDtoResponse>> findAll() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/meus") //os pedidos do cliente autenticado
    public ResponseEntity<List<PedidoDtoResponse>> meusPedidos(@AuthenticationPrincipal Cliente cliente) {
        return ResponseEntity.ok(pedidoService.findByCliente(cliente));
    }

    @GetMapping("/{id}") //o dono do pedido ou o ADMIN (403 para os demais)
    public ResponseEntity<PedidoDtoResponse> findById(@PathVariable Long id, @AuthenticationPrincipal Cliente cliente) {
        return pedidoService.findById(id, cliente)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PedidoDtoResponse> insert(@Valid @RequestBody PedidoDtoRequest dto,
                                                    @AuthenticationPrincipal Cliente cliente,
                                                    UriComponentsBuilder uriBuilder) {
        var pedido = pedidoService.insert(dto, cliente);
        var location = uriBuilder.path("api/v1/pedidos/{id}").buildAndExpand(pedido.id()).toUri();
        return ResponseEntity.created(location).body(pedido); //201 Created (409 se faltar estoque)
    }

    @PatchMapping("/{id}/estado")
    @Secured({"ROLE_ADMIN"}) //só a loja avança o pedido de fase
    public ResponseEntity<PedidoDtoResponse> alteraEstado(@PathVariable Long id, @Valid @RequestBody EstadoPedidoDtoRequest dto) {
        return pedidoService.alteraEstado(id, dto.estado())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelamento") //o dono do pedido ou o ADMIN
    public ResponseEntity<PedidoDtoResponse> cancela(@PathVariable Long id, @AuthenticationPrincipal Cliente cliente) {
        return pedidoService.cancela(id, cliente)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
