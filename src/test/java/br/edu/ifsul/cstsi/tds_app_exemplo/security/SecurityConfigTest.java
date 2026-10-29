package br.edu.ifsul.cstsi.tds_app_exemplo.security;

import br.edu.ifsul.cstsi.tds_app_exemplo.autenticacao.AutenticacaoService;
import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import br.edu.ifsul.cstsi.tds_app_exemplo.infra.jwt.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
    Teste das regras de segurança do app (SecurityConfig + @Secured nos controllers) com MockMvc.

    MockMvc: em vez de subir um servidor web e fazer chamadas HTTP reais (como o TestRestTemplate dos testes de
    integração), o MockMvc executa a requisição dentro de um container Servlet simulado ("mockado"). Toda a cadeia
    do Spring MVC é executada (filtros do Spring Security, controllers, TratadorDeExcecoes), só não há rede.
    É mais rápido e é a forma mais comum de testar a camada web e a segurança.

    O teste está organizado como uma varredura (pente fino) da segurança, em 4 grupos (@Nested):
      1. Rotas públicas (não exigem token);
      2. Login (autenticação);
      3. Sem token ou com token inválido: 401 Unauthorized (quem é você?);
      4. Com token, mas sem o perfil exigido: 403 Forbidden (eu sei quem você é, mas você não pode).

    Os tokens são gerados direto pelo TokenService (sem passar pelo login), a partir dos usuários do data.sql de testes:
    admin@email.com (ROLE_ADMIN), user@email.com (ROLE_USER) e inativo@email.com (ROLE_USER, situacao = false).
 */
@SpringBootTest
@AutoConfigureMockMvc //configura o MockMvc (container com Servlet, mas sem o servidor web)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AutenticacaoService autenticacaoService;
    @Autowired
    private TokenService tokenService;

    private String tokenAdmin;
    private String tokenUser;
    private String tokenInativo;

    @BeforeEach
    void geraTokens() {
        tokenAdmin = tokenService.geraToken((Cliente) autenticacaoService.loadUserByUsername("admin@email.com"));
        tokenUser = tokenService.geraToken((Cliente) autenticacaoService.loadUserByUsername("user@email.com"));
        //simula um token emitido antes de o cliente ser desativado
        tokenInativo = tokenService.geraToken((Cliente) autenticacaoService.loadUserByUsername("inativo@email.com"));
    }

    //adiciona o header Authorization: Bearer <token> na requisição
    private MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String token) {
        return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder requisicao, String corpo) {
        return requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Nested
    @DisplayName("1. Rotas públicas")
    class RotasPublicas {

        @Test
        void documentacaoDaApiNaoExigeToken() throws Exception {
            mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        }

        @Test
        void rotaDeLoginNaoExigeToken() throws Exception {
            //sem token e com dados inválidos: se a rota exigisse token seria 401; como é pública, chega na validação (400)
            mvc.perform(json(post("/api/v1/autenticacao/login"), "{}")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("2. Login")
    class Login {

        private static final String LOGIN = "/api/v1/autenticacao/login";

        @Test
        void usuarioExistenteESenhaCorretaEspera200ComToken() throws Exception {
            mvc.perform(json(post(LOGIN), """
                            { "email": "admin@email.com", "senha": "Teste12@" }
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        void senhaIncorretaEspera401() throws Exception {
            mvc.perform(json(post(LOGIN), """
                            { "email": "admin@email.com", "senha": "Errada12@" }
                            """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }

        @Test
        void usuarioInexistenteEspera401() throws Exception {
            //a mesma resposta da senha incorreta: a API não revela se o e-mail existe ou não
            mvc.perform(json(post(LOGIN), """
                            { "email": "naoexiste@email.com", "senha": "Teste12@" }
                            """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void clienteInativoEspera401() throws Exception {
            //senha correta, mas o cliente está inativo (Cliente#isEnabled() devolve false)
            mvc.perform(json(post(LOGIN), """
                            { "email": "inativo@email.com", "senha": "Teste12@" }
                            """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.title").value("Usuário inativo"));
        }

        @Test
        void dadosInvalidosEspera400ComOsCamposInvalidos() throws Exception {
            mvc.perform(json(post(LOGIN), """
                            { "email": "admin", "senha": "123456" }
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.erros.email").exists())
                    .andExpect(jsonPath("$.erros.senha").exists());
        }
    }

    @Nested
    @DisplayName("3. Sem token ou com token inválido: 401")
    class NaoAutenticado {

        @Test
        void rotasProtegidasSemTokenEsperam401() throws Exception {
            mvc.perform(get("/api/v1/produtos/1")).andExpect(status().isUnauthorized());
            mvc.perform(json(post("/api/v1/produtos"), "{}")).andExpect(status().isUnauthorized());
            mvc.perform(json(put("/api/v1/produtos/1"), "{}")).andExpect(status().isUnauthorized());
            mvc.perform(delete("/api/v1/produtos/1")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/v1/clientes")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/v1/perfis")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/v1/pedidos/meus")).andExpect(status().isUnauthorized());
            mvc.perform(json(post("/api/v1/pedidos"), "{}")).andExpect(status().isUnauthorized());
        }

        @Test
        void tokenInvalidoOuAdulteradoEspera401() throws Exception {
            mvc.perform(comToken(get("/api/v1/produtos/1"), "token.invalido.123")).andExpect(status().isUnauthorized());

            //um token válido com um caractere trocado na assinatura: o JwtDecoder recusa
            var adulterado = tokenUser.substring(0, tokenUser.length() - 2) + (tokenUser.endsWith("A") ? "BB" : "AA");
            mvc.perform(comToken(get("/api/v1/produtos/1"), adulterado)).andExpect(status().isUnauthorized());
        }

        @Test
        void tokenDeClienteInativoEspera401() throws Exception {
            //o token é válido (assinatura e expiração), mas o cliente foi desativado: o CustomJwtAuthenticationConverter recusa
            mvc.perform(comToken(get("/api/v1/produtos/1"), tokenInativo)).andExpect(status().isUnauthorized());
        }

        @Test
        void comTokenValidoAsRotasAbrem() throws Exception {
            mvc.perform(comToken(get("/api/v1/produtos/1"), tokenUser)).andExpect(status().isOk());
            mvc.perform(comToken(get("/api/v1/pedidos/meus"), tokenUser)).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("4. Com token, mas sem o perfil exigido (@Secured): 403")
    class SemPermissao {

        @Test
        void listarTodosOsProdutosExigeAdmin() throws Exception {
            mvc.perform(comToken(get("/api/v1/produtos"), tokenUser)).andExpect(status().isForbidden());
            mvc.perform(comToken(get("/api/v1/produtos"), tokenAdmin)).andExpect(status().isOk());
        }

        @Test
        void clientesEPerfisExigemAdmin() throws Exception {
            mvc.perform(comToken(get("/api/v1/clientes"), tokenUser)).andExpect(status().isForbidden());
            mvc.perform(comToken(get("/api/v1/perfis"), tokenUser)).andExpect(status().isForbidden());
            mvc.perform(comToken(get("/api/v1/clientes"), tokenAdmin)).andExpect(status().isOk());
            mvc.perform(comToken(get("/api/v1/perfis"), tokenAdmin)).andExpect(status().isOk());
        }

        @Test
        void listarTodosOsPedidosEAlterarEstadoExigemAdmin() throws Exception {
            mvc.perform(comToken(get("/api/v1/pedidos"), tokenUser)).andExpect(status().isForbidden());
            mvc.perform(comToken(json(patch("/api/v1/pedidos/1/estado"), """
                            { "estado": "PAGO" }
                            """), tokenUser))
                    .andExpect(status().isForbidden());
            mvc.perform(comToken(get("/api/v1/pedidos"), tokenAdmin)).andExpect(status().isOk());
        }

        @Test
        void respostaDe403SegueOPadraoProblemDetails() throws Exception {
            mvc.perform(comToken(get("/api/v1/clientes"), tokenUser))
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Acesso negado"));
        }
    }
}
