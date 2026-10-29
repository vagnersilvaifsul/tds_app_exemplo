package br.edu.ifsul.cstsi.tds_app_exemplo.perfil;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(String nome);

    boolean existsByNome(String nome);
}
