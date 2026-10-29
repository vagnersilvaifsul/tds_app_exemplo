package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import br.edu.ifsul.cstsi.tds_app_exemplo.infra.exception.RegraDeNegocioException;
import br.edu.ifsul.cstsi.tds_app_exemplo.perfil.Perfil;
import br.edu.ifsul.cstsi.tds_app_exemplo.perfil.PerfilRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
    Camada de serviço do cliente. Ela existe porque o cadastro e a alteração têm regras de negócio:
      1. o e-mail (que é o login) não pode se repetir;
      2. a senha nunca é gravada em texto puro, só o hash BCrypt;
      3. os perfis informados precisam existir (se nenhum for informado, o cliente recebe ROLE_USER).
    As consultas e a exclusão não têm regra de negócio, por isso o ClienteController as faz direto na repository.

    @Transactional: veja a explicação completa sobre transações no PedidoService.
 */
@Service
public class ClienteService {

    private static final String PERFIL_PADRAO = "ROLE_USER";

    private final ClienteRepository clienteRepository;
    private final PerfilRepository perfilRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PerfilRepository perfilRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Cliente insert(ClienteDtoRequest dto) {
        if (dto.senha() == null || dto.senha().isBlank()) {
            throw new RegraDeNegocioException("A senha é obrigatória no cadastro do cliente");
        }
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new RegraDeNegocioException("Já existe um cliente cadastrado com o e-mail " + dto.email());
        }
        var cliente = new Cliente();
        cliente.setNome(dto.nome());
        cliente.setSobrenome(dto.sobrenome());
        cliente.setEmail(dto.email());
        cliente.setSenha(passwordEncoder.encode(dto.senha()));
        cliente.setSituacao(dto.situacao() == null ? Boolean.TRUE : dto.situacao());
        cliente.setPerfis(buscaPerfis(dto.perfis()));
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Optional<Cliente> update(Long id, ClienteDtoRequest dto) {
        return clienteRepository.findById(id).map(cliente -> {
            if (clienteRepository.existsByEmailAndIdNot(dto.email(), id)) {
                throw new RegraDeNegocioException("Já existe outro cliente cadastrado com o e-mail " + dto.email());
            }
            cliente.setNome(dto.nome());
            cliente.setSobrenome(dto.sobrenome());
            cliente.setEmail(dto.email());
            if (dto.senha() != null && !dto.senha().isBlank()) {
                cliente.setSenha(passwordEncoder.encode(dto.senha())); //só troca a senha se uma nova for informada
            }
            if (dto.situacao() != null) {
                cliente.setSituacao(dto.situacao());
            }
            if (dto.perfis() != null && !dto.perfis().isEmpty()) {
                cliente.setPerfis(buscaPerfis(dto.perfis()));
            }
            return cliente; //dentro da transação, as alterações são gravadas no commit (dirty checking), sem save()
        });
    }

    //converte os nomes dos perfis nas entidades Perfil (lança 404 se algum não existir)
    private Set<Perfil> buscaPerfis(List<String> nomes) {
        var nomesPerfis = (nomes == null || nomes.isEmpty()) ? List.of(PERFIL_PADRAO) : nomes;
        var perfis = new LinkedHashSet<Perfil>();
        for (String nome : nomesPerfis) {
            perfis.add(perfilRepository.findByNome(nome)
                    .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado: " + nome)));
        }
        return perfis;
    }
}
