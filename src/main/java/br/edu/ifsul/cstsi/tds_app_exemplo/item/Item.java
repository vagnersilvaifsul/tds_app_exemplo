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
    private Integer quantidade; //Integer, como o estoque do Produto (de onde a quantidade é baixada)
    private BigDecimal totalItem; //quantidade x valor de venda do produto no momento da compra
    private Boolean situacao; //true = ativo, false = cancelado

    /*
        Associações:
          - pedido é LAZY: quem navega é o pedido (pedido.getItems()), o item raramente precisa carregar o pedido.
          - produto continua EAGER (o padrão do @ManyToOne): todo item exibido precisa do nome do produto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "id", nullable = false)
    private Pedido pedido;
    @ManyToOne
    @JoinColumn(name = "produto_id", referencedColumnName = "id", nullable = false)
    private Produto produto;
}
