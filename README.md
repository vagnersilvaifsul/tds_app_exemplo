# Projeto Exemplo sobre construção de API REST com Spring Boot 4.x + IA com MCP

Este projeto demonstra a construção de uma API REST (de uma loja: produtos, clientes e pedidos) com a utilização do
Spring Boot e a inclusão de IA com MCP (Model Context Protocol). É o projeto de exemplo da disciplina Tópicos em
Desenvolvimento de Software (TDS) do Curso Superior de Tecnologia em Sistemas para Internet do IFSul (Câmpus Pelotas).

> **Para estudar o projeto passo a passo, siga o [ROTEIRO.md](ROTEIRO.md):** ele guia você pelos commits, na ordem em
> que o projeto foi construído em aula.

Lista de habilidades exploradas:
1. ORM (Object-Relational Mapping) com Spring Data JPA;
2. Repositories do Spring Data para acesso ao banco de dados;
3. Documentação da API com Spring Doc (OpenAPI + Swagger UI);
4. Construção de Controllers com @RestController e @RequestMapping, e o padrão DTO (Data Transfer Object);
5. Autenticação com o esquema Basic Auth (em memória e banco de dados) e JWT (JSON Web Token, em banco de dados), e autorização por perfil com @Secured;
6. Validação da integridade dos dados (Bean Validation) e tratamento de erros no padrão Problem Details (RFC 9457);
7. Testes de integração das camadas HTTP request -> Security -> Controller -> Service -> Repository -> MariaDB (TestRestTemplate) e testes de segurança com MockMvc;
8. Implantação do projeto no Docker com Dockerfile e Docker-Compose;
9. Integração do projeto com IA através do protocolo MCP (Model Context Protocol).

Conteúdo extra (para quem quiser se aprofundar depois da disciplina):
* CRUD completo de clientes, perfis e pedidos;
* Camada de serviço (Service) e transações com @Transactional (pedido + itens + baixa de estoque, tudo ou nada);
* Estratégias de carregamento (LAZY × EAGER), @EntityGraph e o problema N+1.

## Requisitos

* Java 17 (o SDK pode ser instalado pelo IntelliJ);
* MariaDB (recomenda-se a versão 11.x em Docker): `docker run -d --name mariadb -e MARIADB_ROOT_PASSWORD=mariadb -p 3306:3306 mariadb:11.8`;
* IntelliJ IDEA (ou o Maven Wrapper do projeto: `./mvnw`).

Configurações (em `src/main/resources/application.properties`):
* Banco de dados `tads_app_exemplo` (criado automaticamente), usuário `root` e senha da variável de ambiente `MARIADB_SECRET` (padrão: `mariadb`);
* Secret do JWT na variável de ambiente `JWT_SECRET` (o valor padrão serve apenas para as aulas).

## Para executar o projeto basta:
1. Executar o docker-compose.yaml (`docker compose up --build`);
2. Ou utilizar a IDE IntelliJ (no perfil default) e executar o projeto pelo lançador de apps;
3. Ou executar o comando: `./mvnw spring-boot:run`;
4. Ou executar o comando: `java -jar target/tds_app_exemplo-1.0.jar` (depois de gerar o jar com `./mvnw clean package`).

Na primeira execução, o `data.sql` popula o banco com produtos, perfis, clientes e pedidos de exemplo.

## Para testar a API

1. Acesse a documentação em http://localhost:8080/swagger-ui.html;
2. Faça o login em `POST /api/v1/autenticacao/login` com um dos usuários abaixo e copie o token da resposta;
3. Clique em **Authorize**, cole o token e chame os demais end-points.

| Usuário | Senha | Perfil |
|---|---|---|
| admin@email.com | Teste12@ | ROLE_ADMIN |
| user@email.com | Teste12@ | ROLE_USER |

Para gerar o hash BCrypt de uma nova senha (para o `data.sql`), execute o `main()` da classe `Cliente`.

| Recurso | End-points | Quem acessa |
|---|---|---|
| Autenticação | `POST /api/v1/autenticacao/login` | Público |
| Produtos | `GET /{id}`, `GET /nome/{nome}`, `POST`, `PUT /{id}`, `DELETE /{id}` | Autenticado (`GET /api/v1/produtos`, a lista completa, só ADMIN) |
| Clientes | CRUD em `/api/v1/clientes` | ADMIN |
| Perfis | CRUD em `/api/v1/perfis` | ADMIN |
| Pedidos | `POST`, `GET /meus`, `GET /{id}`, `PATCH /{id}/cancelamento` | Autenticado (o dono do pedido ou o ADMIN) |
| Pedidos | `GET /api/v1/pedidos`, `PATCH /{id}/estado` | ADMIN |

Os erros seguem o padrão Problem Details (RFC 9457), com o Content-Type `application/problem+json`. Exemplo:

```json
{ "type": "about:blank", "title": "Regra de negócio violada", "status": 409,
  "detail": "Estoque insuficiente para o produto Café (disponível: 1, solicitado: 2)", "instance": "/api/v1/pedidos" }
```

## Testes

* Os testes usam um banco **físico** (`tads_app_exemplo_teste`, no mesmo MariaDB), configurado em `src/test/resources/application-test.properties`.
  É um recurso didático, para que você possa consultar o banco e rastrear o que cada teste fez. Em uma empresa, os testes
  rodariam em um banco em memória ou em um container descartável (ex.: Testcontainers).
* Para executar todos: `./mvnw test` (ou o plugin `test` do Maven no IntelliJ).
* Para ver a cobertura: no IntelliJ, botão direito em `src/test/java` > **More Run/Debug > Run 'All Tests' with Coverage**.

## Para testar a conexão MCP basta executar um dos comandos abaixo:
1. Basta executar o teste de unidade ProdutoMcpToolsTest que está no pacote mcp da pasta src/test/java, ou
2. `npx @modelcontextprotocol/inspector --cli --config mcp.json --server tds-app-exemplo --method tools/list`, ou
3. `npx @modelcontextprotocol/inspector --cli --config mcp.json --server tds-app-exemplo --method tools/call --tool-name buscarProdutoPorNome --tool-arg nome=a`

Porém, instale antes o MCP Inspector com o comando: `npm install -g @modelcontextprotocol/inspector` (e deixe o app rodando).
