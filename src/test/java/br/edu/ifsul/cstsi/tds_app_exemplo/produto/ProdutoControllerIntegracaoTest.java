package br.edu.ifsul.cstsi.tds_app_exemplo.produto;


import br.edu.ifsul.cstsi.tds_app_exemplo.BaseAPIIntegracaoTest;
import br.edu.ifsul.cstsi.tds_app_exemplo.TdsAppExemploApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
    Realiza o teste de integração da unidade ProdutoController.
    Utiliza como dependência principal a classe TestRestTemplate (do Spring), implementada na superclasse BaseAPIIntegracaoTest.

    O teste de integração testa todas as camadas HTTP request -> Security -> Controller -> Repository -> MariaDB.
 */

@SpringBootTest(classes = TdsAppExemploApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
//carrega o Context do app em um container Spring Boot com um servidor web
@ActiveProfiles("test") //indica o profile que o Spring Boot deve utilizar para passar os testes
public class ProdutoControllerIntegracaoTest extends BaseAPIIntegracaoTest {

    //Métodos utilitários (eles encapsulam o TestRestTemplate e eliminam a repetição de código nos casos de teste)
    private ResponseEntity<ProdutoDtoResponse> getProduto(String url) {
        return get(url, ProdutoDtoResponse.class);
    }

    //Métodos utilitários (eles encapsulam o TestRestTemplate e eliminam a repetição de código nos casos de teste)
    private ResponseEntity<List<ProdutoDtoResponse>> getProdutosList(String url) {
        var headers = getHeaders();

        return rest.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                });
    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    @DisplayName("Espera uma página com 5 objetos e uma página de 5 objetos") //dá para detalhar o que vai aparecer ao no resultado do teste
    public void findAllEsperaUmaPaginaCom5ObjetosEUmaPaginaDe5Objetos() { //O nome do método de teste é importante porque deve transmitir a essência do que ele verifica. Este não é um requisito técnico, mas sim uma oportunidade de capturar informações.
        // ACT
        var data = getProdutosList("/api/v1/produtos").getBody();

        // ASSERT (testa se retorna a quantidade de dados esperada)
        assertNotNull(data);
        assertEquals(5, data.size());

        // ACT - testa uma requisição com os parametrôs page e size
        var dataPage = getProdutosList("/api/v1/produtos?page=0&size=5").getBody();

        // ASSERT (testa se retorna o tamanho de página solicitado)
        assertNotNull(dataPage);
        assertEquals(5, dataPage.size());
    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    public void findByIdEsperaUmObjetoPorIdPesquisadoENotFoudParaIdInexistente() {
        // ACT + ASSERT
        assertNotNull(getProduto("/api/v1/produtos/1"));
        assertNotNull(getProduto("/api/v1/produtos/2"));
        assertNotNull(getProduto("/api/v1/produtos/3"));
        assertNotNull(getProduto("/api/v1/produtos/4"));
        assertNotNull(getProduto("/api/v1/produtos/5"));
        assertEquals(HttpStatus.NOT_FOUND, getProduto("/api/v1/produtos/100000").getStatusCode());
    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    public void findByNomeEsperaUmObjetoPorNomePesquisado() {
        // ACT + ASSERT
        assertEquals(1, getProdutosList("/api/v1/produtos/nome/arroz").getBody().size());
        assertEquals(1, getProdutosList("/api/v1/produtos/nome/erva").getBody().size());
        assertEquals(1, getProdutosList("/api/v1/produtos/nome/cha").getBody().size());
        assertEquals(1, getProdutosList("/api/v1/produtos/nome/cafe").getBody().size());
        assertEquals(1, getProdutosList("/api/v1/produtos/nome/feijao").getBody().size());

        // ACT + ASSERT
        assertEquals(HttpStatus.NO_CONTENT, getProdutosList("/api/v1/produtos/nome/xxx").getStatusCode());
    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    public void testInsertEspera201CreatedE404ENotFound() {
        // ARRANGE
        var ProdutoDTOPost = new ProdutoDtoPost(
                "Teste",
                "Desc. do produto Teste",
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                100
        );

        // ACT
        var response = post("/api/v1/produtos", ProdutoDTOPost, null);

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var location = response.getHeaders().get("location").get(0);
        var p = getProduto(location).getBody();
        assertNotNull(p);
        assertEquals("Teste", p.nome());
        assertEquals("Desc. do produto Teste", p.descricao());
        assertEquals(new BigDecimal("10.00"), p.valorDeVenda());
        assertEquals(Integer.valueOf(100), p.estoque());

        // ACT
        delete(location, null);

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, getProduto(location).getStatusCode());
    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    public void testUpdateEspera200OkE404ENotFound() {
        // ARRANGE
        var ProdutoDTOPost = new ProdutoDtoPost(
                "Teste",
                "Desc. do produto Teste",
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                100
        );

        var responsePost = post("/api/v1/produtos", ProdutoDTOPost, null);
        assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        var location = responsePost.getHeaders().get("location").get(0);
        var pDto = getProduto(location).getBody();
        assertNotNull(pDto);
        assertEquals("Teste", pDto.nome());
        assertEquals("Desc. do produto Teste", pDto.descricao());
        assertEquals(new BigDecimal("10.00"), pDto.valorDeVenda());
        assertEquals(Integer.valueOf(100), pDto.estoque());
        //prepara um DTO para o PUT
        var produtoDTOPut = new ProdutoDtoPut(
                "Teste Modificado",
                "Desc. do produto Teste Modificado",
                new BigDecimal("20.00"),
                new BigDecimal("50.00"),
                500,
                Boolean.FALSE
        );

        // ACT
        var responsePUT = put(location, produtoDTOPut, ProdutoDtoResponse.class);

        // ASSERT
        assertEquals(HttpStatus.OK, responsePUT.getStatusCode());
        assertEquals("Teste Modificado", responsePUT.getBody().nome());
        assertEquals("Desc. do produto Teste Modificado", responsePUT.getBody().descricao());
        assertEquals(new BigDecimal("50.00"), responsePUT.getBody().valorDeVenda());
        assertEquals(Integer.valueOf(500), responsePUT.getBody().estoque());

        // ACT
        delete(location, null);

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, getProduto(location).getStatusCode());

    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    public void testDeleteEspera200OkE404NotFound() {
        // ARRANGE
        var produto = new Produto();
        produto.setNome("Teste");
        produto.setDescricao("Desc. do produto Teste");
        produto.setValorDeCompra(new BigDecimal("5.00"));
        produto.setValorDeVenda(new BigDecimal("10.00"));
        produto.setEstoque(100);
        produto.setSituacao(true);
        var responsePost = post("/api/v1/produtos", produto, null);
        assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        var location = responsePost.getHeaders().get("location").get(0);
        var p = getProduto(location).getBody();
        assertNotNull(p);
        assertEquals("Teste", p.nome());
        assertEquals(Integer.valueOf(100), p.estoque());

        // ACT
        var responseDelete = delete(location, null);

        // ASSERT
        assertEquals(HttpStatus.OK, responseDelete.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, getProduto(location).getStatusCode());
    }

    @Test //Esta anotação JUnit sinaliza que este método é um caso de teste
    public void testGetNotFoundEspera404NotFound() {
        // ARRANGE + ACT
        var response = getProduto("/api/v1/produtos/1100");

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}