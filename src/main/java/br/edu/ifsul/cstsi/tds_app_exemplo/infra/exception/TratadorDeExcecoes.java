package br.edu.ifsul.cstsi.tds_app_exemplo.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/*
    Esta classe centraliza o tratamento de exceções do app: em vez de cada controller tratar os erros, eles "sobem"
    até aqui e são convertidos em uma resposta HTTP com o status adequado.

    Formato das respostas de erro: Problem Details (RFC 9457), o padrão adotado pelo Spring Boot desde a versão 3 e o
    formato nativo do Spring Framework 7 (Spring Boot 4). O corpo tem o Content-Type application/problem+json:
        {
          "type": "about:blank",
          "title": "Recurso não encontrado",
          "status": 404,
          "detail": "Produto não encontrado: 100",
          "instance": "/api/v1/pedidos"
        }
    Campos extras podem ser adicionados com setProperty (ex.: "erros" na validação).

    Por que estender ResponseEntityExceptionHandler?
    Ela já trata, no padrão Problem Details, todas as exceções do próprio Spring MVC: JSON mal formatado (400),
    parâmetro de tipo errado ou ausente (400), método HTTP não suportado (405), tipo de mídia não suportado (415),
    rota inexistente (404)... Assim, esta classe só precisa tratar as exceções da NOSSA aplicação, e sobrescrever
    a validação para listar os campos inválidos.

    De onde vem esta implementação (o formato "correto" segundo a documentação oficial):
      - RFC 9457 - Problem Details for HTTP APIs (o padrão do corpo de erro):
        https://www.rfc-editor.org/rfc/rfc9457
      - Spring Framework 7, "Error Responses" (ProblemDetail, ErrorResponse e ResponseEntityExceptionHandler):
        https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
      - Spring Framework 7, "Exceptions" (métodos @ExceptionHandler em um @RestControllerAdvice):
        https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html
      - Spring Boot 4, "Error Handling" (propriedade spring.mvc.problemdetails.enabled, alternativa a esta classe
        quando não há exceções próprias para tratar):
        https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.error-handling

    O formato abaixo é o esqueleto recomendado, e é exatamente o que esta classe segue:

        @RestControllerAdvice
        public class TratadorDeExcecoes extends ResponseEntityExceptionHandler { // 1. herda o tratamento das exceções do Spring MVC

            @Override                                                           // 2. personaliza uma exceção do Spring MVC
            protected ResponseEntity<Object> handleMethodArgumentNotValid(...) {
                ProblemDetail problema = ex.getBody();
                problema.setProperty("erros", ...);
                return handleExceptionInternal(ex, problema, headers, status, request);
            }

            @ExceptionHandler(MinhaExcecao.class)                                // 3. trata as exceções da aplicação
            public ProblemDetail handleMinhaExcecao(MinhaExcecao ex) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
            }
        }

    Lembrete: as exceções lançadas nos filtros do Spring Security (ex.: requisição sem token, 401) não chegam aqui;
    elas são tratadas pelo próprio Spring Security antes de a requisição chegar aos controllers.
 */
@RestControllerAdvice //adiciona ao Context do app como um componente que intercepta as exceções lançadas pelos controllers
public class TratadorDeExcecoes extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeExcecoes.class);

    /*
        400 Bad Request: DTO anotado com @Valid que não passou na validação.
        O Spring já monta o ProblemDetail; aqui só acrescentamos o mapa campo -> mensagem, para o cliente da API
        saber exatamente o que corrigir. Não esqueça do @Valid nos parâmetros dos controllers que recebem DTOs.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = ex.getBody(); //já vem com status 400
        problema.setTitle("Erro de validação");
        problema.setDetail("Um ou mais campos da requisição são inválidos");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    //404 Not Found: recurso buscado (ou referenciado, ex.: o produto de um item do pedido) não existe
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    //401 Unauthorized: falha na autenticação feita pelo controller (ex.: e-mail ou senha incorretos no login)
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", "E-mail ou senha incorretos");
    }

    //403 Forbidden: usuário autenticado, mas sem permissão (@Secured ou regra de dono do recurso)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage());
    }

    //409 Conflict: regra de negócio violada (ex.: estoque insuficiente, e-mail já cadastrado)
    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail handleRegraDeNegocio(RegraDeNegocioException ex) {
        return problema(HttpStatus.CONFLICT, "Regra de negócio violada", ex.getMessage());
    }

    /*
        409 Conflict: o banco recusou a operação (ex.: excluir um registro referenciado por outro, valor único repetido).
        A mensagem original do banco vai só para o log: ela expõe nomes de tabelas e colunas, e não deve sair da API.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Violação de integridade de dados",
                "A operação não pode ser concluída porque o registro está vinculado a outros dados ou repete um valor único");
    }

    //monta o ProblemDetail; o Spring preenche o "instance" com o caminho da requisição
    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        var problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
