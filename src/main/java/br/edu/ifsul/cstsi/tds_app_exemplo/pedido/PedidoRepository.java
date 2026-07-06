package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}