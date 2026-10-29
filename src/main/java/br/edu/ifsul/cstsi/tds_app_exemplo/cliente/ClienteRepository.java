package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id); //existe OUTRO cliente com este e-mail?
}
