-- Script de dados iniciais. A ordem dos INSERTs segue as dependências entre as tabelas (chaves estrangeiras):
-- primeiro as tabelas que não dependem de ninguém, depois as que apontam para elas.
--   1. produtos e perfis (independentes)
--   2. clientes          (independente)
--   3. clientes_perfis   (depende de clientes e perfis)
--   4. pedidos           (depende de clientes)
--   5. itens             (depende de pedidos e produtos)
-- Como o script roda a cada inicialização (spring.sql.init.mode=always), os INSERTs repetidos falham por chave
-- primária duplicada e são ignorados (spring.sql.init.continue-on-error=true).

-- 1. Produtos e perfis
INSERT INTO produtos (id, nome, descricao, estoque, valor_de_compra, valor_de_venda, situacao)
VALUES (1, 'Café', 'Café em pó tradicional Igaçu lata 400g', 100, 5.00, 10.00, true),
       (2, 'Erva Mate', 'Erva Mate Pura Folha 1kg', 100, 5.00, 10.00, true),
       (3, 'Chá Preto', 'Chá Preto Prenda cx 10 unidades', 100, 5.00, 10.00, true),
       (4, 'Arroz', 'Arroz Camil branco polido tipo 1 pacote 5kg', 100, 5.00, 10.00, true),
       (5, 'Feijão', 'Feijão Tordilho pacote 1kg', 100, 5.00, 10.00, true);

INSERT INTO perfis (id, nome)
VALUES (1, 'ROLE_ADMIN'),
       (2, 'ROLE_USER');

-- 2. Clientes (a senha de ambos é Teste12@; para gerar o hash de outra senha, execute o main() da classe Cliente)
INSERT INTO clientes (id, nome, sobrenome, email, senha, is_confirmado, situacao)
VALUES (1, 'Admin', 'do Sistema', 'admin@email.com', '$2a$10$GNPFCV7./ZvNwdY8mCBofu7gsBnK1lVUErPLX1qM9W/ljW52sVa6W', true, true),
       (2, 'Usuario', 'do Sistema', 'user@email.com', '$2a$10$GNPFCV7./ZvNwdY8mCBofu7gsBnK1lVUErPLX1qM9W/ljW52sVa6W', true, true);

-- 3. Perfis de cada cliente (tabela de junção do @ManyToMany; tem chave primária composta (clientes_id, perfis_id))
INSERT INTO clientes_perfis (clientes_id, perfis_id)
VALUES (1, 1),
       (2, 2);

-- 4. Pedidos (totais calculados como quantidade x valor de venda dos itens abaixo)
--    Pedido 1: entregue | Pedido 2: pago, aguardando separação | Pedido 3: cancelado (situacao = false)
INSERT INTO pedidos (id, pagamento, estado, data_criacao, data_modificacao, situacao, total_pedido, cliente_id)
VALUES (1, 'PIX', 'ENTREGUE', '2026-10-01', '2026-10-05', true, 30.00, 2),
       (2, 'CARTAO', 'PAGO', '2026-10-20', '2026-10-21', true, 30.00, 2),
       (3, 'BOLETO', 'NOVO', '2026-10-25', '2026-10-26', false, 10.00, 1);

-- 5. Itens de cada pedido
INSERT INTO itens (id, quantidade, total_item, situacao, pedido_id, produto_id)
VALUES (1, 2, 20.00, true, 1, 1),   -- pedido 1: 2 x Café
       (2, 1, 10.00, true, 1, 4),   -- pedido 1: 1 x Arroz
       (3, 3, 30.00, true, 2, 2),   -- pedido 2: 3 x Erva Mate
       (4, 1, 10.00, false, 3, 5);  -- pedido 3 (cancelado): 1 x Feijão
