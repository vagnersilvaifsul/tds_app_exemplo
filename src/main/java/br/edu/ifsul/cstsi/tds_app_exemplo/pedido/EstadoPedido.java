package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

/*
    Fase em que o pedido se encontra. A ordem das constantes é a ordem do fluxo do pedido.
    O cancelamento não é um estado: é a situação do pedido (Pedido#situacao = false), que pode acontecer
    enquanto o pedido ainda não foi enviado.

    No banco é gravado como texto (ver @Enumerated(EnumType.STRING) em Pedido). Nunca use EnumType.ORDINAL: ele grava a
    posição (0, 1, 2...) e, se alguém reordenar as constantes, os dados gravados passam a significar outra coisa.
 */
public enum EstadoPedido {
    NOVO,
    PAGO,
    EM_SEPARACAO,
    ENVIADO,
    ENTREGUE
}
