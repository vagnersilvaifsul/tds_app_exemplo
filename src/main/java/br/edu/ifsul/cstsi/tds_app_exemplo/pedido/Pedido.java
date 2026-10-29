package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import br.edu.ifsul.cstsi.tds_app_exemplo.item.Item;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pagamento;
    @Enumerated(EnumType.STRING) //grava o nome da constante (NOVO, PAGO...) e não a sua posição
    @Column(length = 20)
    private EstadoPedido estado;
    private LocalDate dataCriacao;
    private LocalDate dataModificacao;
    private Boolean situacao; //true = ativo, false = cancelado
    private BigDecimal totalPedido;

    /*
        Associações:
          - items é LAZY: os itens só são buscados quando o código acessa getItems() (dentro de uma transação, ver PedidoService).
          - cascade = PERSIST: ao salvar um pedido novo, o JPA salva também os itens que estão na lista. Assim o pedido e
            seus itens são gravados juntos, na mesma transação, sem precisar de uma ItemRepository.save() para cada item.
     */
    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Item> items = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false)
    private Cliente cliente;
}
