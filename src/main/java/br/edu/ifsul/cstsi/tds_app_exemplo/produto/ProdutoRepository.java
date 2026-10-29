package br.edu.ifsul.cstsi.tds_app_exemplo.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/*
    O Padrão Repository do Spring Data, apesar de nos fornecer uma inferface Domain Speak, inteligente o
    suficiente para montar o SQL da consulta para nós em tempo de execução, é flexível o bastante para nos permitir
    utilizar as nossas próprias Query Speak (é o caso do SQL apresentado na anotação @Query). Ou seja, o Spring Data nos
    permite alternar entre Domain Speak e Query Speak rapidamente. É muita flexibilidade!
 */

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    /*
        As consultas que retornam coleções devolvem List (vazia quando não encontram nada). Não é preciso envolver uma
        List em Optional: Optional é para consultas que retornam um único objeto (ex.: o findById da JpaRepository).
     */

    //Método em Domain Speak (só pelo nome do método o Spring Data irá injetar o SQL em tempo de execução)
    List<Produto> findByNomeStartingWith(String nome);

    //Método em Query Speak (em JPQL). Note o nome da entidade Produto no JPQL, isso mostra que ele roda no objeto JPA.
    @Query("SELECT p FROM Produto p WHERE p.nome LIKE CONCAT(?1, '%')")
    List<Produto> findByNomeQuerySpeakJPQL(String nome);

    //Método em Query Speak (em SQL). Note que utiliza o tradicional SQl, inclusive o nome da tabela é a que está no banco de dados.
    @Query(value = "SELECT p.* FROM produtos p WHERE p.nome LIKE CONCAT(?1, '%') ORDER BY p.nome", nativeQuery = true)
    List<Produto> findByNomeQuerySpeakSQL(String nome);
}