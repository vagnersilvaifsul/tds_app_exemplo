package br.edu.ifsul.cstsi.tds_app_exemplo.mcp;

import br.edu.ifsul.cstsi.tds_app_exemplo.infra.mcp.ProdutoMcpTools;
import br.edu.ifsul.cstsi.tds_app_exemplo.produto.ProdutoDtoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/*
    Testa as ferramentas MCP chamando o bean diretamente (o protocolo MCP em si é testado com o MCP Inspector,
    como descrito no README). Usa a massa de dados de src/test/resources/data.sql.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProdutoMcpToolsTest {

    @Autowired
    private ProdutoMcpTools tools;

    @Test
    void listarProdutosDevolveOsProdutosDaMassaDeTestes() {
        var produtos = tools.listarProdutos();
        assertTrue(produtos.size() >= 5); //os 5 do data.sql (outros testes podem estar criando produtos)
        assertTrue(produtos.stream().anyMatch(p -> p.nome().equals("Café")));
    }

    @Test
    void buscarProdutoPorNomeDevolveOsQueComecamComOTexto() {
        assertEquals(1, tools.buscarProdutoPorNome("Caf").size());
        assertTrue(tools.buscarProdutoPorNome("xyz").isEmpty());
    }

    @Test
    void asFerramentasNaoExpoemOValorDeCompra() {
        //o DTO devolvido pelas ferramentas não tem o componente valorDeCompra (dado interno da loja)
        assertTrue(Arrays.stream(ProdutoDtoResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .noneMatch("valorDeCompra"::equals));
    }
}
