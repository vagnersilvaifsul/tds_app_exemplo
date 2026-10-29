package br.edu.ifsul.cstsi.tds_app_exemplo.perfil;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/*
    CRUD de perfis (extra). Não há regra de negócio nem transação envolvendo mais de uma operação, por isso o controller
    acessa a repository diretamente (sem camada de serviço).

    O @Secured na classe vale para todos os métodos: só o ADMIN gerencia perfis.
    Excluir um perfil que ainda está associado a algum cliente viola a chave estrangeira da tabela clientes_perfis:
    o TratadorDeExcecoes converte essa DataIntegrityViolationException em 409 Conflict.
 */
@RestController
@RequestMapping("api/v1/perfis")
@Secured({"ROLE_ADMIN"})
public class PerfilController {

    private final PerfilRepository perfilRepository;

    public PerfilController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @GetMapping
    public ResponseEntity<List<PerfilDtoResponse>> findAll() {
        return ResponseEntity.ok(perfilRepository.findAll().stream().map(PerfilDtoResponse::new).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilDtoResponse> findById(@PathVariable Long id) {
        return perfilRepository.findById(id)
                .map(perfil -> ResponseEntity.ok(new PerfilDtoResponse(perfil)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PerfilDtoResponse> insert(@Valid @RequestBody PerfilDtoRequest dto, UriComponentsBuilder uriBuilder) {
        var perfil = new Perfil();
        perfil.setNome(dto.nome());
        perfil = perfilRepository.save(perfil); //nome repetido viola o unique da coluna: 409 Conflict (TratadorDeExcecoes)
        var location = uriBuilder.path("api/v1/perfis/{id}").buildAndExpand(perfil.getId()).toUri();
        return ResponseEntity.created(location).body(new PerfilDtoResponse(perfil));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerfilDtoResponse> update(@PathVariable Long id, @Valid @RequestBody PerfilDtoRequest dto) {
        return perfilRepository.findById(id)
                .map(perfil -> {
                    perfil.setNome(dto.nome());
                    return ResponseEntity.ok(new PerfilDtoResponse(perfilRepository.save(perfil)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (perfilRepository.existsById(id)) {
            perfilRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
