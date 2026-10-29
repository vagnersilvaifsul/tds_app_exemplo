package br.edu.ifsul.cstsi.tds_app_exemplo.perfil;

import br.edu.ifsul.cstsi.tds_app_exemplo.BaseAPIIntegracaoTest;
import br.edu.ifsul.cstsi.tds_app_exemplo.TdsAppExemploApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/*
    Teste de integração da unidade PerfilController (extra).
    Cada teste exclui o que criou, para não interferir nas execuções seguintes (o banco de testes é físico).
 */
@SpringBootTest(classes = TdsAppExemploApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class PerfilControllerIntegracaoTest extends BaseAPIIntegracaoTest {

    private static final String URL = "/api/v1/perfis";

    @Test
    public void findByIdEspera200E404() {
        assertEquals("ROLE_ADMIN", get(URL + "/1", PerfilDtoResponse.class).getBody().nome());
        assertEquals(HttpStatus.NOT_FOUND, get(URL + "/100000", String.class).getStatusCode());
    }

    @Test
    public void insertUpdateEDeleteEsperam201200E200() {
        // ACT (insert)
        var response = post(URL, new PerfilDtoRequest("ROLE_TESTE"), PerfilDtoResponse.class);

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var location = response.getHeaders().getLocation().toString();

        // ACT + ASSERT (nome repetido viola o unique: 409)
        assertEquals(HttpStatus.CONFLICT, post(URL, new PerfilDtoRequest("ROLE_TESTE"), String.class).getStatusCode());

        // ACT + ASSERT (update)
        var alterado = put(location, new PerfilDtoRequest("ROLE_TESTE_ALTERADO"), PerfilDtoResponse.class);
        assertEquals(HttpStatus.OK, alterado.getStatusCode());
        assertEquals("ROLE_TESTE_ALTERADO", alterado.getBody().nome());

        // ACT + ASSERT (delete)
        assertEquals(HttpStatus.OK, delete(location, Void.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, get(location, String.class).getStatusCode());
    }

    @Test
    public void insertComNomeForaDoPadraoEspera400() {
        var response = post(URL, new PerfilDtoRequest("admin"), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("ROLE_NOME"));
    }

    @Test
    public void deleteDePerfilEmUsoEspera409() {
        //o perfil ROLE_ADMIN (id 1) está associado ao admin@email.com na tabela clientes_perfis
        assertEquals(HttpStatus.CONFLICT, delete(URL + "/1", String.class).getStatusCode());
    }
}
