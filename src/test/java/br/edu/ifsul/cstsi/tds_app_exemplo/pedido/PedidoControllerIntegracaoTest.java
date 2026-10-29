package br.edu.ifsul.cstsi.tds_app_exemplo.pedido;

import br.edu.ifsul.cstsi.tds_app_exemplo.BaseAPIIntegracaoTest;
import br.edu.ifsul.cstsi.tds_app_exemplo.TdsAppExemploApplication;
import br.edu.ifsul.cstsi.tds_app_exemplo.item.ItemDtoRequest;
import br.edu.ifsul.cstsi.tds_app_exemplo.produto.ProdutoDtoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
    Teste de integração da unidade PedidoController (extra), com foco na TRANSAÇÃO do PedidoService:
    o pedido, os itens e a baixa de estoque acontecem juntos ou não acontecem.

    Como os testes alteram o estoque (e o banco de testes não é recriado a cada execução), as verificações são
    RELATIVAS: lê o estoque antes, executa a ação e compara. Ao final, cada teste cancela o pedido criado, o que
    devolve o estoque e deixa o banco como estava.
 */
@SpringBootTest(classes = TdsAppExemploApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class PedidoControllerIntegracaoTest extends BaseAPIIntegracaoTest {

    private static final String URL = "/api/v1/pedidos";

    //Métodos utilitários
    private Integer estoqueDoProduto(long produtoId) {
        return get("/api/v1/produtos/" + produtoId, ProdutoDtoResponse.class).getBody().estoque();
    }

    private ResponseEntity<PedidoDtoResponse> patch(String url, Object body) {
        return rest.exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, getHeaders()), PedidoDtoResponse.class);
    }

    //quando a resposta esperada é um erro, lê o corpo como String (o corpo é um ProblemDetail, montado pelo TratadorDeExcecoes)
    private ResponseEntity<String> patchComErro(String url, Object body) {
        return rest.exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, getHeaders()), String.class);
    }

    private ResponseEntity<PedidoDtoResponse> criaPedido(ItemDtoRequest... itens) {
        return post(URL, new PedidoDtoRequest("PIX", List.of(itens)), PedidoDtoResponse.class);
    }

    private ResponseEntity<String> criaPedidoComErro(ItemDtoRequest... itens) {
        return post(URL, new PedidoDtoRequest("PIX", List.of(itens)), String.class);
    }

    @Test
    @DisplayName("Criar pedido calcula os totais e baixa o estoque; cancelar devolve o estoque")
    public void insertBaixaEstoqueECancelamentoDevolve() {
        // ARRANGE
        var estoqueAntes = estoqueDoProduto(5);

        // ACT
        var response = criaPedido(new ItemDtoRequest(5L, 2));

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var pedido = response.getBody();
        assertNotNull(pedido);
        assertEquals(EstadoPedido.NOVO, pedido.estado());
        assertEquals(Boolean.TRUE, pedido.situacao());
        assertEquals(1, pedido.itens().size());
        assertEquals(0, new BigDecimal("20.00").compareTo(pedido.totalPedido())); //2 x 10.00 (calculado no servidor)
        assertEquals(estoqueAntes - 2, estoqueDoProduto(5));

        // ACT (cancelamento)
        var cancelado = patch(URL + "/" + pedido.id() + "/cancelamento", null);

        // ASSERT
        assertEquals(HttpStatus.OK, cancelado.getStatusCode());
        assertEquals(Boolean.FALSE, cancelado.getBody().situacao());
        assertEquals(estoqueAntes, estoqueDoProduto(5)); //estoque devolvido

        // ACT + ASSERT (não pode cancelar duas vezes)
        assertEquals(HttpStatus.CONFLICT, patchComErro(URL + "/" + pedido.id() + "/cancelamento", null).getStatusCode());
    }

    @Test
    @DisplayName("Rollback: se um item não tem estoque, nenhum item do pedido baixa estoque")
    public void insertComEstoqueInsuficienteEspera409EDesfazTudo() {
        // ARRANGE
        var estoqueProduto4Antes = estoqueDoProduto(4);
        var estoqueProduto5Antes = estoqueDoProduto(5);

        // ACT (o 1º item tem estoque e é processado; o 2º não tem e lança a exceção)
        var response = criaPedidoComErro(new ItemDtoRequest(4L, 1), new ItemDtoRequest(5L, 1_000_000));

        // ASSERT
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(estoqueProduto4Antes, estoqueDoProduto(4)); //a baixa do 1º item foi desfeita (ROLLBACK)
        assertEquals(estoqueProduto5Antes, estoqueDoProduto(5));
    }

    @Test
    public void insertComProdutoInexistenteEspera404() {
        assertEquals(HttpStatus.NOT_FOUND, criaPedidoComErro(new ItemDtoRequest(100_000L, 1)).getStatusCode());
    }

    @Test
    @DisplayName("O estado do pedido só avança de fase")
    public void alteraEstadoSoAvanca() {
        // ARRANGE
        var pedido = criaPedido(new ItemDtoRequest(3L, 1)).getBody();
        assertNotNull(pedido);

        // ACT + ASSERT
        var pago = patch(URL + "/" + pedido.id() + "/estado", new EstadoPedidoDtoRequest(EstadoPedido.PAGO));
        assertEquals(HttpStatus.OK, pago.getStatusCode());
        assertEquals(EstadoPedido.PAGO, pago.getBody().estado());
        assertEquals(HttpStatus.CONFLICT, patchComErro(URL + "/" + pedido.id() + "/estado", new EstadoPedidoDtoRequest(EstadoPedido.NOVO)).getStatusCode());

        // limpeza: cancela o pedido para devolver o estoque
        assertEquals(HttpStatus.OK, patch(URL + "/" + pedido.id() + "/cancelamento", null).getStatusCode());
    }

    @Test
    public void meusPedidosEFindByIdEsperam200E404() {
        // ARRANGE
        var pedido = criaPedido(new ItemDtoRequest(2L, 1)).getBody();
        assertNotNull(pedido);

        // ACT + ASSERT
        assertEquals(HttpStatus.OK, get(URL + "/" + pedido.id(), PedidoDtoResponse.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, get(URL + "/100000", String.class).getStatusCode());
        var meus = rest.exchange(URL + "/meus", HttpMethod.GET, new HttpEntity<>(getHeaders()), PedidoDtoResponse[].class);
        assertEquals(HttpStatus.OK, meus.getStatusCode());
        assertTrue(List.of(meus.getBody()).stream().anyMatch(p -> p.id().equals(pedido.id())));

        // limpeza
        patch(URL + "/" + pedido.id() + "/cancelamento", null);
    }
}
