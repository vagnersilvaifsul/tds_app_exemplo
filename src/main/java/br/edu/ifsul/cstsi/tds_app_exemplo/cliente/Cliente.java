package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import br.edu.ifsul.cstsi.tds_app_exemplo.pedido.Pedido;
import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String sobrenome;
    @Column(unique = true)
    private String email;
    private String senha;
    private boolean isConfirmado = false;
    private Byte situacao;

    //Associações
    @OneToMany(mappedBy = "cliente", fetch = FetchType.EAGER)
    private Collection<Pedido> pedidos;
}
