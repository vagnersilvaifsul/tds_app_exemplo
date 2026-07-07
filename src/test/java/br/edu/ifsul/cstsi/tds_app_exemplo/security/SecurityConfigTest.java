package br.edu.ifsul.cstsi.tds_app_exemplo.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    Se você nunca ouviu falar, Mocking é uma forma de teste onde, em vez de verificar os resultados, verificamos os
    métodos invocados.
 */

@SpringBootTest
@AutoConfigureMockMvc //Autoconfigura o Spring Boot Web, modo Mockado (o que siginifica nos entregar um container com um Servlet, mas sem o servidor web)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired //@AutoConfigureMockMvc (anotação na linha 16) nos permite injetar esse Bean
    private MockMvc mvc; //Elimina a necessidade de um servidor e nos permite realizar chamadas "Mockadas" nos end-points

    @Test
    void endpointLoginQuandoUsuarioExistenteESenhaCorretaEspera200Ok() throws Exception {
        //ARRANGE
        var json = """
                {
                  "email": "admin@email.com",
                  "senha": "Teste12@"
                }
                """;
        var url = "/api/v1/autenticacao/login";

        //ACT
        var response = mvc.perform( //performa uma requisição
                post(url) //verbo na rota
                        .content(json) //o body da requisição
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(200, response.getStatus());
    }


    @Test
    void endpointLoginQuandoUsuarioInexistenteEspera403NaoAutorizado() throws Exception {
        //ARRANGE
        var json = """
                {
                  "email": "admin",
                  "senha": "Teste12@"
                }
                """;
        var url = "/api/v1/autenticacao/login";

        //ACT
        var response = mvc.perform( //performa uma requisição
                post(url) //verbo na rota
                        .content(json) //o body da requisição
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(403, response.getStatus());
    }

    @Test
    void endpointLoginQuandoUsuarioExistenteESenhaIncorretaEspera403NaoAutorizado() throws Exception {
        //ARRANGE
        var json = """
                {
                  "email": "admin@email.com",
                  "senha": "123456"
                }
                """;
        var url = "/api/v1/autenticacao/login";

        //ACT
        var response = mvc.perform( //performa uma requisição
                post(url) //verbo na rota
                        .content(json) //o body da requisição
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(403, response.getStatus());
    }

    @Test
    void endpointCadastrarProdutoQuandoVerboGetPutEDeleteEspera401Forbidden() throws Exception {
        //ARRANGE
        var json = """
                {
                   "nome": "Teste",
                   "descricao": "Desc Teste",
                   "valorDeCompra": 2.00,
                   "valorDeVenda": 4.00,
                   "estoque": 100
                 }
                """;
        var url = "/api/v1/produtos";

        //ACT + ASSERT
        this.mvc.perform(get(url)).andExpect(status().isUnauthorized());

        //ACT
        var response = mvc.perform( //performa uma requisição
                put(url) //verbo na rota
                        .content(json) //o body da requisição
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(401, response.getStatus());

        //ACT
        response = mvc.perform( //performa uma requisição
                delete(url) //verbo na rota
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(401, response.getStatus());
    }

    @Test
    void endPointProdutosQuandoVerboGetPostPutDeleteENaoEstaAutenticadoEspera401NaoAutenticado() throws Exception {
        //ARRANGE
        var url = "/api/v1/produtos";
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUEkgUHJvZHV0b3MgRXhlbXBsbyBkZSBUQURTIiwic3ViIjoiYWRtaW5AZW1haWwuY29tIiwiaWF0IjoxNzE0NzYxMDMxfQ.aQCggW0xCjEtsHDqJGxeu-5lkrrevFrjLZSl9aHDTrI";
        var body = """
                {
                  "key": "value",
                  "key2": "value2"
                }
                """;

        //ACT + ASSERT
        this.mvc.perform(get(url)) //verbo na rota
                .andExpect(status().isUnauthorized());

        //ACT
        var response = mvc.perform( //performa uma requisição
                post(url) //verbo na rota
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(401, response.getStatus());

        //ACT
        response = mvc.perform( //performa uma requisição
                put(url) //verbo na rota
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(401, response.getStatus());

        //ACT
        response = mvc.perform( //performa uma requisição
                delete(url) //verbo na rota
                        .contentType(MediaType.APPLICATION_JSON) //o header Content-type
        ).andReturn().getResponse(); //a response da requisição

        //ASSERT
        Assertions.assertEquals(401, response.getStatus());
    }
}