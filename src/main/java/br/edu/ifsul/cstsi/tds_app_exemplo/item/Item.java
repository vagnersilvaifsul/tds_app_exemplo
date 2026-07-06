package br.edu.ifsul.cstsi.tds_app_exemplo.item;

import br.edu.ifsul.cstsi.tds_app_exemplo.pedido.Pedido;
import br.edu.ifsul.cstsi.tds_app_exemplo.produto.Produto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "itens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal quantidade;
    private BigDecimal totalItem;
    private Byte situacao;

    //Associações
    @ManyToOne
    @JoinColumn(name = "pedido_id", referencedColumnName = "id", nullable = false)
    private Pedido pedido;
    @ManyToOne
    @JoinColumn(name = "produto_id", referencedColumnName = "id", nullable = false)
    private Produto produto;
}
