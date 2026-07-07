INSERT INTO produtos (id, nome, descricao, estoque, valor_de_compra, valor_de_venda, situacao)
VALUES (1, 'Café', 'Café em pó tradicional Igaçu lata 400g', 100, 5.00, 10.00, true),
       (2, 'Erva Mate', 'Erva Mate Pura Folha 1kg', 100, 5.00, 10.00, true),
       (3, 'Chá Preto', 'Prenda', 100, 5.00, 10.00, true),
       (4, 'Arroz', 'Arroz Camil branco polido tipo 1 pacote 5kg', 100, 5.00, 10.00, true),
       (5, 'Feijão', 'Feijão Tordilho pacote 1kg', 100, 5.00, 10.00, true);

insert into perfis(id, nome)
values (1, 'ROLE_ADMIN');
insert into perfis(id, nome)
values (2, 'ROLE_USER');

insert into clientes(id, nome, sobrenome, email, senha, is_confirmado, situacao)
values (1, 'Admin', 'do Sistema', 'admin@email.com', '$2a$10$GNPFCV7./ZvNwdY8mCBofu7gsBnK1lVUErPLX1qM9W/ljW52sVa6W',true, true);
insert into clientes(id, nome, sobrenome, email, senha, is_confirmado, situacao)
values (2, 'Usuario', 'do Sistema', 'user@email.com', '$2a$10$GNPFCV7./ZvNwdY8mCBofu7gsBnK1lVUErPLX1qM9W/ljW52sVa6W',true, true);

insert into clientes_perfis(clientes_id, perfis_id)
values (1, 1);
insert into clientes_perfis(clientes_id, perfis_id)
values (2, 2);