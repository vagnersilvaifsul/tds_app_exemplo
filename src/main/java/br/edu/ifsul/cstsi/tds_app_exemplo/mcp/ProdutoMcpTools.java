package br.edu.ifsul.cstsi.tds_app_exemplo.mcp;

import br.edu.ifsul.cstsi.tds_app_exemplo.produto.Produto;
import br.edu.ifsul.cstsi.tds_app_exemplo.produto.ProdutoRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/*
    Capstone MCP: expõe o domínio Produto (já construído pelos alunos nas semanas anteriores) como
    ferramentas MCP, para que um cliente de IA (Claude Desktop, Claude Code) consiga consultá-lo em
    linguagem natural.

    Repare que nenhuma anotação nova foi necessária no ProdutoRepository e nada mudou no
    ProdutoController — o Spring AI lê a assinatura de cada método e a description de cada
    @McpToolParam para gerar o JSON Schema sozinho. É essa descrição (não o nome do parâmetro) que o
    modelo usa para decidir quando e como chamar cada ferramenta — trate-a como documentação para o
    modelo, não só para humanos.

    Ponto de atenção de segurança (discutir em aula): esta classe chama o ProdutoRepository diretamente,
    não o ProdutoController. Por isso, a anotação @Secured({"ROLE_ADMIN"}) que protege
    ProdutoController#findAll() NÃO se aplica aqui — são caminhos de código diferentes. Segurança em
    nível de método não "vaza" automaticamente para quem chama o repository por outro caminho.
 */
@Service
public class ProdutoMcpTools {

    private final ProdutoRepository produtoRepository;

    public ProdutoMcpTools(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @McpTool(description = "Lista todos os produtos cadastrados, com nome, preço de venda, preço de compra e estoque atual")
    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    @McpTool(description = "Busca produtos cujo nome começa com o texto informado")
    public List<Produto> buscarProdutoPorNome(
            @McpToolParam(description = "Início do nome do produto, ex: 'caf' para Café", required = true) String nome) {
        return produtoRepository.findByNomeStartingWith(nome).orElse(List.of());
    }
}
