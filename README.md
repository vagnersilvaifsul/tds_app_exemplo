# Projeto Exemplo sobre construção de API REST com Spring Boot 4x + IA com MCP

Este projeto demonstra a construção de API REST com a utilização do Spring Boot e a inclusão de IA com MCP.

Lista de habilidades exploradas:
1. ORM com Spring Data;
2. Construção de Controllers com Spring REST e Repositories para acesso ao banco de dados;
3. Documentação da API com Spring Doc;
4. Construção de Controllers com @RestController e @RequestMapping;
5. Autenticação com o esquema Basic Auth (em memória e banco de dados) e JWT (em banco de dados);
6. Validação da Integridade do Banco de Dados;
7. Construção de testes de unidade, escrevendo testes para testar as camadas integradas HTTP request -> Security -> Controller -> Repository -> MariaDB;
8. Implantação do projeto no Docker com Dockerfile e Docker-Compose;
9. Integracão do projeto com IA através do protocolo MCP (Model Context Protocol).

Para executar o projeto basta executar o docker-compose.yaml ou utilizar a IDE IntelliJ e executar o projeto.

Para testar a conexão MCP basta executar um dos comandos abaixo:
1. npx @modelcontextprotocol/inspector --cli --config mcp.json --server tds-app-exemplo --method tools/list, ou
2. npx @modelcontextprotocol/inspector --cli --config mcp.json --server tds-app-exemplo --method tools/call --tool-name buscarProdutoPorNome --tool-arg nome=a
Porém, instale antes o MCP Inspector com o comando: npm install -g @modelcontextprotocol/inspector
