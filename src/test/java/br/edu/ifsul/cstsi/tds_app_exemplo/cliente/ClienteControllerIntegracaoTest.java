package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import br.edu.ifsul.cstsi.tds_app_exemplo.BaseAPIIntegracaoTest;
import br.edu.ifsul.cstsi.tds_app_exemplo.TdsAppExemploApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
    Teste de integração da unidade ClienteController (extra), com foco nas regras do ClienteService:
    e-mail único, senha obrigatória no cadastro (e nunca devolvida pela API) e perfis existentes.
    Cada teste exclui o cliente que criou (o banco de testes é físico).
 */
@SpringBootTest(classes = TdsAppExemploApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class ClienteControllerIntegracaoTest extends BaseAPIIntegracaoTest {

    private static final String URL = "/api/v1/clientes";

    private ClienteDtoRequest novoCliente(String email, String senha) {
        return new ClienteDtoRequest("Teste", "da Silva", email, senha, List.of("ROLE_USER"), null);
    }

    @Test
    public void insertEspera201SemDevolverASenhaE409ParaEmailRepetido() {
        // ACT
        var response = post(URL, novoCliente("teste.cliente@email.com", "Teste12@"), String.class);

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertFalse(response.getBody().contains("senha")); //nem a senha nem o hash saem da API
        var location = response.getHeaders().getLocation().toString();
        var cliente = get(location, ClienteDtoResponse.class).getBody();
        assertNotNull(cliente);
        assertEquals(List.of("ROLE_USER"), cliente.perfis());
        assertEquals(Boolean.TRUE, cliente.situacao()); //situação não informada: nasce ativo

        // ACT + ASSERT (e-mail repetido: regra de negócio, 409)
        assertEquals(HttpStatus.CONFLICT, post(URL, novoCliente("teste.cliente@email.com", "Teste12@"), String.class).getStatusCode());

        // limpeza
        assertEquals(HttpStatus.OK, delete(location, Void.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, get(location, String.class).getStatusCode());
    }

    @Test
    public void insertSemSenhaEspera409EComPerfilInexistenteEspera404() {
        assertEquals(HttpStatus.CONFLICT, post(URL, novoCliente("sem.senha@email.com", null), String.class).getStatusCode());

        var comPerfilInexistente = new ClienteDtoRequest("Teste", "da Silva", "perfil.inexistente@email.com", "Teste12@",
                List.of("ROLE_NAO_EXISTE"), null);
        assertEquals(HttpStatus.NOT_FOUND, post(URL, comPerfilInexistente, String.class).getStatusCode());
    }

    @Test
    public void updateSemSenhaMantemASenhaEEspera200() {
        // ARRANGE
        var location = post(URL, novoCliente("teste.update@email.com", "Teste12@"), String.class)
                .getHeaders().getLocation().toString();

        // ACT (senha null: mantém a atual)
        var response = put(location, new ClienteDtoRequest("Alterado", "da Silva", "teste.update@email.com", null, null, false),
                ClienteDtoResponse.class);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alterado", response.getBody().nome());
        assertEquals(Boolean.FALSE, response.getBody().situacao());
        assertEquals(List.of("ROLE_USER"), response.getBody().perfis()); //perfis não informados: mantém os atuais
        assertEquals(HttpStatus.NOT_FOUND, put(URL + "/100000", novoCliente("x@email.com", null), String.class).getStatusCode());

        // limpeza
        delete(location, Void.class);
    }

    @Test
    public void insertComDadosInvalidosEspera400() {
        var response = post(URL, new ClienteDtoRequest("", "", "email-invalido", "123", null, null), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("email"));
        assertTrue(response.getBody().contains("senha"));
    }
}
