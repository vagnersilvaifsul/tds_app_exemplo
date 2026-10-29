# Roteiro de estudo — TDS Exemplo

Este roteiro guia você pela construção do projeto **commit a commit**, na mesma ordem das aulas. Cada etapa corresponde
a uma tarefa do Plano de Ensino. A ideia não é copiar o código: é entender **o que mudou em cada commit e por quê**, e
então aplicar o mesmo passo no seu próprio projeto.

## Como usar este roteiro

1. **Veja o que mudou em um commit:**
   * no IntelliJ: janela **Git** (Alt+9 / ⌘9) > aba **Log** > clique no commit > painel da direita mostra os arquivos alterados (duplo clique abre o *diff*);
   * no terminal: `git show <hash>` (ex.: `git show 92f8d79`).
2. **Volte o projeto inteiro para um commit** (para executar o app como ele estava naquele momento):
   `git checkout <hash>`. Para voltar ao estado atual: `git checkout main`.
3. **Compare dois momentos:** `git diff <hash-antigo> <hash-novo>`.
4. **Use as branches de cada tarefa:** `git checkout tarefa4_autenticacao` mostra o projeto como ficou ao final da tarefa 4.

Para cada commit, faça três perguntas: *o que mudou?*, *por que mudou?* e *como eu testo essa mudança?*
(no navegador, no Swagger UI em http://localhost:8080/swagger-ui.html ou nos testes).

> **Atenção:** os commits antigos mostram o projeto **como ele era** naquele momento, inclusive decisões que foram
> revistas depois (ex.: o Spring Data REST, removido no fim). As seções 9 e 10 explicam o que mudou e por quê.

## Visão geral

| Etapa | Tarefa | Branch | Semanas | Tema |
|---|---|---|---|---|
| 1 | T1 | `tarefa1_orm` | 1 a 3 | ORM com Spring Data JPA |
| 2 | T2 | `tarefa2_springrest_e_doc` | 4 | Repositories e documentação da API |
| 3 | T3 | `tarefa3_construcao_controller` | 6 | Controllers e DTOs |
| 4 | T4 | `tarefa4_autenticacao` | 7 a 9 | Spring Security: Basic, @Secured e JWT |
| 5 | T5 | `tarefa5_validacao` | 11 | Validação e tratamento de erros |
| 6 | T6 | `tarefa6_testes` | 13 e 14 | Testes de integração |
| 7 | T7 | `tarefa7_implantacao_docker` | 16 | Docker |
| 8 | T8 | `tarefa8_ia_mcp` | 17 | IA com MCP |
| 9 | — | `main` | extra | CRUD completo, Service e transações |
| 10 | — | `main` | extra | Correções e boas práticas |

---

## Etapa 1 — ORM com Spring Data JPA (T1)

**Objetivo:** criar o projeto Spring Boot, conectá-lo ao MariaDB e mapear as classes do domínio (Produto, Cliente, Pedido e Item) em tabelas.

| Commit | O que observar |
|---|---|
| `18d62bc` | Projeto criado (Spring Initializr): o `pom.xml` com os starters e a classe `TdsAppExemploApplication`. Execute e veja o Tomcat subir. |
| `8ac030d` | Configuração da conexão no `application.properties` (URL, usuário, senha) e do `ddl-auto`. |
| `92f8d79` | As entidades: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@OneToMany`. Compare as classes com as tabelas criadas no banco. |
| `4fc80c0` | O `data.sql` e as propriedades `spring.sql.init.*` que o executam na inicialização. |

**Conceitos-chave:** mapeamento objeto-relacional, chave primária e estrangeira, associações (1:N, N:1, N:N), `ddl-auto=update`.

**Verifique:** abra o banco (ex.: no Database do IntelliJ) e encontre as tabelas, as colunas e as chaves estrangeiras geradas a partir das anotações.

---

## Etapa 2 — Repositories e documentação da API (T2)

**Objetivo:** acessar o banco com o padrão Repository e documentar a API com o Spring Doc.

| Commit | O que observar |
|---|---|
| `e427bbd` | As interfaces Repository (`extends JpaRepository<Entidade, Long>`): o CRUD pronto, sem implementar nada. |
| `3d3e511` | O Spring Doc no `pom.xml` e a documentação em `/swagger-ui.html`. |
| `f8355c9` e `f1ea967` | **Histórico:** o Spring Data REST (controllers gerados a partir das repositories) e o problema de compatibilidade com o Spring Doc. Ele foi **removido** no commit `e0e5ee0` (Etapa 10). |

**Conceitos-chave:** padrão Repository, *Domain Speak* (consulta pelo nome do método) e *Query Speak* (JPQL e SQL nativo com `@Query`), OpenAPI.

**Verifique:** no `ProdutoRepository`, compare as três formas da mesma busca por nome (Domain Speak, JPQL e SQL nativo).

---

## Etapa 3 — Controllers e DTOs (T3)

**Objetivo:** construir o `ProdutoController` à mão, verbo a verbo, e separar o que entra e sai da API com DTOs.

| Commit | O que observar |
|---|---|
| `a466fcd` | O Lombok (`@Getter`, `@Setter`, construtores) eliminando o código repetitivo das entidades. |
| `1747875` | Métodos de consulta na repository, prontos para o controller. |
| `beb727a` | Um controller "de mentira", que só devolve textos: teste cada verbo HTTP (GET, POST, PUT, DELETE) no Swagger. |
| `ee5e9fa` | O `ResponseEntity`: o tipo que encapsula a resposta HTTP inteira (status, headers e corpo). |
| `0abfb34` | A injeção de dependência da repository e a lógica de cada método (ainda sem DTO). |
| `7a15af9` e `cfff01c` | O padrão DTO (Data Transfer Object) com `record`. |

**Conceitos-chave:** `@RestController`, `@RequestMapping`, verbos e status HTTP (200, 201, 204, 404), injeção de dependência pelo construtor, princípio da responsabilidade única (SRP).

**Verifique:** por que o `ProdutoDtoResponse` não tem o `valorDeCompra`? (resposta no comentário da classe)

> A versão atual dos DTOs de produto (um de entrada, `ProdutoDtoRequest`, e um de saída) está no commit `eab85a8` (Etapa 10).

---

## Etapa 4 — Spring Security: Basic, @Secured e JWT (T4)

**Objetivo:** proteger a API, evoluindo da configuração padrão até o JWT com usuários do banco.

| Commit | O que observar |
|---|---|
| `b2193d2` | Só o starter de segurança: tudo passa a exigir login (usuário `user` e senha impressa no console). |
| `9bcb905` | O `SecurityConfig` com Basic Auth e usuários **em memória**. |
| `9a4a43e` | O `@Secured({"ROLE_ADMIN"})` com o `@EnableMethodSecurity`: autorização por perfil no método (Global Method). |
| `bd77fd6` | Usuários **no banco de dados**: o `Cliente` implementa `UserDetails`, e o `AutenticacaoService`, `UserDetailsService`. |
| `95a55c6` | A troca do Basic pelo **JWT** nativo do Spring Security (OAuth2 Resource Server): `JwtConfig`, `TokenService`, login. |
| `b5acad0` | O botão **Authorize** do Swagger, para enviar o token JWT. |
| `8b00a23`, `4285a1f` e `8e67e11` | Refatoração: as classes de JWT reunidas no pacote `infra/jwt`, e os ajustes decorrentes. |

**Conceitos-chave:** autenticação (quem é você? → 401) × autorização (o que você pode? → 403), BCrypt, sessão *stateless*, JWT (cabeçalho, claims e assinatura).

**Verifique:** cole o token do login em https://jwt.io e identifique o `sub` (e-mail) e o `exp` (expiração).

---

## Etapa 5 — Validação e tratamento de erros (T5)

**Objetivo:** recusar dados inválidos antes que cheguem ao banco e centralizar o tratamento de erros.

| Commit | O que observar |
|---|---|
| `2b569d2` | As anotações do Bean Validation nos DTOs (`@NotBlank`, `@Size`, `@Pattern`...), o `@Valid` nos controllers e o `TratadorDeExcecoes` (`@RestControllerAdvice`). |
| `375d74c` | O `@Valid` que faltava no login. |

**Conceitos-chave:** validação na entrada da API, `@RestControllerAdvice` e `@ExceptionHandler`.

> O `TratadorDeExcecoes` foi reescrito no padrão Problem Details (RFC 9457) no commit `9159257` (Etapa 10): estude as duas versões e compare.

---

## Etapa 6 — Testes de integração (T6)

**Objetivo:** testar as camadas juntas, com requisições HTTP reais (TestRestTemplate) e um banco de testes físico.

| Commit | O que observar |
|---|---|
| `6af643f` | O `BaseAPIIntegracaoTest` (gera o token e encapsula os verbos HTTP), o `ProdutoControllerIntegracaoTest` e, como curiosidade, o `SecurityConfigTest` com MockMvc. |

**Conceitos-chave:** pirâmide de testes, padrão AAA (Arrange, Act, Assert), `@SpringBootTest`, profile de teste (`application-test.properties`).

**Verifique:** execute os testes com cobertura (botão direito em `src/test/java` > **More Run/Debug > Run 'All Tests' with Coverage**) e encontre uma linha vermelha (não testada).

---

## Etapa 7 — Docker (T7)

| Commit | O que observar |
|---|---|
| `d93d56e` | O `Dockerfile` *multi-stage* (uma imagem para compilar, outra, menor, para executar) e o `docker-compose.yaml` (app + MariaDB). |

**Conceitos-chave:** imagem × container, camadas e cache do Docker, rede interna do compose (o host do banco é `mariadb`, e não `localhost`).

**Verifique:** `docker compose up --build` e acesse o Swagger na porta 8080.

---

## Etapa 8 — IA com MCP (T8)

| Commit | O que observar |
|---|---|
| `d444954` | O starter do servidor MCP do Spring AI, as ferramentas `@McpTool` (`ProdutoMcpTools`), a rota `/mcp` liberada no `SecurityConfig` e o `mcp.json`. |
| `c3cf4ab` | A classe movida para o pacote `infra/mcp`. |
| `1f9eed4` | As ferramentas passam a devolver o `ProdutoDtoResponse` (o modelo de IA recebe só o que a API exporia). |

**Conceitos-chave:** MCP (Model Context Protocol), ferramentas descritas para o modelo (a `description` é documentação **para a IA**), transporte Streamable-HTTP.

**Verifique:** execute o `ProdutoMcpToolsTest` e, com o app rodando, os comandos do MCP Inspector descritos no README.

---

## Etapa 9 — Extra: CRUD completo, Service e transações

Depois da disciplina, estes commits completam o sistema. Os commits marcados com `extra_tarefaX` aprofundam a tarefa X.

| Commit | O que observar |
|---|---|
| `0f575dc` | `extra_tarefa1`: **LAZY × EAGER** (leia os comentários em `Cliente`, `Pedido` e `Item`), o enum `EstadoPedido` e o `@Enumerated(EnumType.STRING)`. |
| `da66444` | `extra_tarefa3`: CRUD de perfis. Sem regra de negócio, o controller acessa a repository diretamente. |
| `8e18d90` | `extra_tarefa3`: CRUD de clientes com **camada de serviço** (`ClienteService`), porque há regras de negócio: e-mail único, senha com BCrypt, perfis. |
| `770ef34` | `extra_tarefa3`: pedidos e itens com `PedidoService` e **`@Transactional`**. Leia o comentário sobre transações no início da classe. |
| `e71677a` | `extra_tarefa6`: o teste do **rollback**: um pedido com um item sem estoque não baixa o estoque de nenhum item. |
| `2abec4d` | `extra_tarefa1`: o `data.sql` na ordem das dependências entre as tabelas, com pedidos de exemplo. |
| `c7e4475`, `ccb924a` e `8b05257` | `extra_tarefa6`: testes de clientes, perfis e das regras de pedido (dono do pedido, produto inativo, pedido cancelado ou enviado). |

**Conceitos-chave:** quando criar um Service (regra de negócio ou transação), transação ACID (tudo ou nada), *dirty checking*, `cascade`, `@EntityGraph` e o problema N+1, regra de autorização que depende do dado (o dono do pedido).

**Verifique:** crie um pedido no Swagger, consulte o estoque do produto, cancele o pedido e consulte o estoque de novo.

---

## Etapa 10 — Correções e boas práticas

Cada correção abaixo é uma lição sobre um erro comum. Leia a mensagem do commit e o *diff*.

| Commit | Lição |
|---|---|
| `eab85a8` | DTOs padronizados (um de entrada, outro de saída) e um PUT que não cria um registro novo quando o id não existe. |
| `e0e5ee0` | Remoção do Spring Data REST: com controllers próprios, ele só expunha dados indevidos (inclusive o hash da senha dos clientes). |
| `9159257` | Erros no padrão **Problem Details (RFC 9457)**, estendendo o `ResponseEntityExceptionHandler`; validação com 400 (não 403) e login com 401. |
| `1004ef1` | **List × Set** em `@ManyToMany`: com `List`, a tabela de junção fica sem chave primária e o `data.sql` duplicava os registros. |
| `c49e803` e `b378cad` | O `SecurityConfigTest` como uma varredura da segurança (rotas públicas, login, 401 e 403). |
| `a009fa7` | Cliente inativo não autentica (`isEnabled()`), nem com um token emitido antes de ser desativado. |
| `a013081` | Testes das ferramentas MCP. |
| `caed8da` | O `.gitignore` para o relatório de cobertura. |

---

## Glossário

| Sigla / termo | Significado |
|---|---|
| ACID | Atomicidade, Consistência, Isolamento e Durabilidade (propriedades de uma transação) |
| API | Application Programming Interface (Interface de Programação de Aplicações) |
| AAA | Arrange, Act, Assert (organização de um caso de teste: preparar, executar, verificar) |
| BCrypt | Algoritmo de hash de senhas (a senha nunca é gravada em texto puro) |
| CRUD | Create, Read, Update, Delete (as quatro operações básicas sobre os dados) |
| CSRF | Cross-Site Request Forgery (ataque que explora sessões com cookies; desligado em APIs stateless) |
| DTO | Data Transfer Object (objeto que define o que entra e sai da API) |
| EAGER / LAZY | Carregamento ansioso (sempre junto) / preguiçoso (só quando acessado) de uma associação |
| JPA | Jakarta Persistence API (especificação de ORM do Java; o Hibernate é a implementação) |
| JPQL | Jakarta Persistence Query Language (consultas sobre as entidades, não sobre as tabelas) |
| JWT | JSON Web Token (token assinado que identifica o usuário a cada requisição) |
| MCP | Model Context Protocol (protocolo para conectar modelos de IA a ferramentas e dados) |
| N+1 | Problema de desempenho: 1 consulta para a lista + N consultas, uma para cada item |
| ORM | Object-Relational Mapping (Mapeamento Objeto-Relacional) |
| REST | Representational State Transfer (estilo de arquitetura de APIs sobre HTTP) |
| RFC 9457 | Problem Details for HTTP APIs (padrão do corpo das respostas de erro) |
| SRP | Single-Responsibility Principle (Princípio da Responsabilidade Única) |
