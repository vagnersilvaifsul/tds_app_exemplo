package br.edu.ifsul.cstsi.tds_app_exemplo.produto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}