package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /*
        @EntityGraph: como items é LAZY, listar N pedidos e depois acessar os itens de cada um geraria 1 consulta para
        os pedidos + N consultas para os itens (o problema "N+1"). O EntityGraph pede ao JPA que traga os itens junto,
        nesta consulta específica, sem tornar a associação EAGER para o resto do sistema.
     */
    @Override
    @EntityGraph(attributePaths = {"items"})
    List<Pedido> findAll();

    @EntityGraph(attributePaths = {"items"})
    List<Pedido> findByClienteIdOrderByIdDesc(Long clienteId);
}
