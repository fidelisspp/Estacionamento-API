insert into vagas (id, codigo, status) values (600, 'A-06', 'OCUPADA');

insert into clientes_tem_vagas
(numero_recibo, placa, marca, modelo, cor, data_entrada, id_cliente, id_vaga)
values ('20260101-120000', 'SRV-1000', 'FIAT', 'UNO', 'PRETO', LOCALTIMESTAMP, 21, 600);

insert into CLIENTES_VAGAS_SERVICOS (id_cliente_vaga, id_servico)
select id, 10 from clientes_tem_vagas where numero_recibo = '20260101-120000';
insert into CLIENTES_VAGAS_SERVICOS (id_cliente_vaga, id_servico)
select id, 30 from clientes_tem_vagas where numero_recibo = '20260101-120000';
