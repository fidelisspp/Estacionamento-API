insert into USUARIOS (id, username, password, role) values (100, 'ana@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_ADMIN');
insert into USUARIOS (id, username, password, role) values (101, 'bia@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role) values (102, 'bob@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role) values (103, 'toby@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_CLIENTE');

insert into CLIENTES (id, nome, cpf, id_usuario) values (10, 'Bianca Silva', '79074426050', 101);
insert into CLIENTES (id, nome, cpf, id_usuario) values (20, 'Roberto Gomes', '55352517047', 102);

insert into VAGAS (id, codigo, status) values (10, 'A-01', 'LIVRE');
insert into clientes_tem_vagas (numero_recibo, placa, marca, modelo, cor, data_entrada, data_saida, valor, desconto, id_cliente, id_vaga)
values ('20230314-101400', 'SIE-1020', 'FIAT', 'SIENA', 'BRANCO', '2023-03-14 10:15:00', '2023-03-14 11:15:00', 9.25, 0.00, 20, 10);
