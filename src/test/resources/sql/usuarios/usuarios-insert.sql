insert into USUARIOS (id, username, password, role) values (100, 'ana@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_ADMIN');
insert into USUARIOS (id, username, password, role) values (101, 'bia@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role) values (102, 'bob@email.com', '$2a$10$AtWo422MdyRQ1RgPzmJNnuDB7xN0GW38sXT4rnBFBqGnMyVmVEf4O', 'ROLE_CLIENTE');

insert into CLIENTES (id, nome, cpf, id_usuario) values (10, 'Bianca Silva', '79074426050', 101);
