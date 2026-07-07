package br.edu.ifsul.cstsi.tds_app_exemplo.autenticacao;

import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import org.springframework.data.repository.Repository;

/*
    Esta interface visa, única e exclusivamente, realizar a busca pelo usuário com UserDetails.
    Note que ela implementa Repository, ao invés de JpaRepository. Assim, a AutenticacaoRepository
    vem vazia, sem métodos CRUD, como a JpaRepository. Logo, essa interface só terá o(s) método(s)
    implementado (s)nela.
    A responsabilidade por CRUD de usuários fica a cargo da ClienteRepository.
 */
public interface AutenticacaoRepository extends Repository<Cliente,Long> {
    Cliente findByEmail(String email);
}
