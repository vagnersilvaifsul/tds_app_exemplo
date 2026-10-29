package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/*
    CRUD de clientes (extra). Só o ADMIN gerencia clientes (@Secured na classe).

    Repare na divisão: as consultas e a exclusão vão direto na ClienteRepository (não têm regra de negócio), enquanto
    o cadastro e a alteração passam pelo ClienteService (que tem as regras de e-mail único, senha com BCrypt e perfis).
    Excluir um cliente que tem pedidos viola a chave estrangeira: 409 Conflict (TratadorDeExcecoes).
 */
@RestController
@RequestMapping("api/v1/clientes")
@Secured({"ROLE_ADMIN"})
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;

    public ClienteController(ClienteRepository clienteRepository, ClienteService clienteService) {
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDtoResponse>> findAll() {
        return ResponseEntity.ok(clienteRepository.findAll().stream().map(ClienteDtoResponse::new).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDtoResponse> findById(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> ResponseEntity.ok(new ClienteDtoResponse(cliente)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClienteDtoResponse> insert(@Valid @RequestBody ClienteDtoRequest dto, UriComponentsBuilder uriBuilder) {
        var cliente = clienteService.insert(dto);
        var location = uriBuilder.path("api/v1/clientes/{id}").buildAndExpand(cliente.getId()).toUri();
        return ResponseEntity.created(location).body(new ClienteDtoResponse(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDtoResponse> update(@PathVariable Long id, @Valid @RequestBody ClienteDtoRequest dto) {
        return clienteService.update(id, dto)
                .map(cliente -> ResponseEntity.ok(new ClienteDtoResponse(cliente)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
