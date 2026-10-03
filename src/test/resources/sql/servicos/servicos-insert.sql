insert into USUARIOS (id, username, password, role) values (100, 'ana@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_ADMIN');
insert into USUARIOS (id, username, password, role) values (101, 'bia@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_CLIENTE');

insert into CLIENTES (id, nome, cpf, id_usuario) values (21, 'Beatriz Rodrigues', '09191773016', 101);

insert into VAGAS (id, codigo, status) values (100, 'A-01', 'OCUPADA');

insert into SERVICOS (id, nome, descricao, preco, tipo) values (10, 'Lavagem simples', 'Lavagem externa', 30.00, 'LAVAGEM');
insert into SERVICOS (id, nome, descricao, preco, tipo) values (20, 'Lavagem completa', 'Lavagem externa e interna', 60.00, 'LAVAGEM');
insert into SERVICOS (id, nome, descricao, preco, tipo) values (30, 'Manobrista', 'Estaciona o carro para o cliente', 15.00, 'MANOBRISTA');

insert into CLIENTES_TEM_VAGAS (id, numero_recibo, placa, marca, modelo, cor, data_entrada, id_cliente, id_vaga)
values (1, '20230313-101300', 'FIT-1020', 'FIAT', 'PALIO', 'VERDE', '2023-03-13 10:15:00', 21, 100);

insert into CLIENTES_VAGAS_SERVICOS (id_cliente_vaga, id_servico) values (1, 30);
