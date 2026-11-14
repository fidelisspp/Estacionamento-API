# Estacionamento-API

API REST para gestão de um estacionamento de veículos: cadastro de usuários e clientes, controle de vagas, serviços extras, registro de entrada (check-in) e saída (check-out) dos veículos, com cálculo automático do valor a pagar.

Projeto final da disciplina, desenvolvido com Spring Boot.

- **Parte 1:** entidades relacionadas, CRUD completo, paginação, consultas personalizadas, documentação OpenAPI e HATEOAS.
- **Parte 2 (versão 2.0.0):** chave de API obrigatória por aplicativo, planos com rate limiting, idempotência em requisições `POST` e versionamento de API por header.

**Versão atual:** 2.0.0. O histórico de mudanças está no [CHANGELOG.md](CHANGELOG.md).

## API em produção (Render)

| Recurso | Endereço |
|---|---|
| Swagger UI | https://estacionamento-api-6yn3.onrender.com/docs-park.html |
| Base da API | https://estacionamento-api-6yn3.onrender.com/api/v1 |

Todas as rotas `/api/**` exigem o header `X-API-Key` (veja [Headers](#headers)). A chave de produção não fica no repositório. Ela é entregue à parte, junto com o projeto.

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem |
| Spring Boot 4.1 | Framework base (Web MVC, Validation, Security) |
| Spring Data JPA + Hibernate | Persistência (ORM) |
| H2 Database | Banco de dados em arquivo (`./data/estacionamento`) |
| Spring HATEOAS | Links hipermídia nas respostas (formato HAL) |
| Springdoc OpenAPI 3 / Swagger UI | Documentação interativa |
| JJWT 0.13 | Autenticação por token JWT |
| Bucket4j | Rate limiting por plano (token bucket) |
| ModelMapper + Lombok | Conversão entidade ↔ DTO e redução de código repetitivo |
| JUnit 5 + RestTestClient | Testes de integração ponta a ponta |
| Maven (wrapper incluso) | Build e dependências |

## Como executar

**Pré-requisito:** JDK 17 ou superior. Não é preciso instalar o Maven, porque o projeto usa o Maven Wrapper.

A aplicação precisa de quatro variáveis de ambiente:

| Variável | Para quê |
|---|---|
| `JWT_SECRET` | Segredo que assina os tokens JWT. Mínimo de 32 caracteres. |
| `ADMIN_USERNAME` | E-mail do administrador inicial. |
| `ADMIN_PASSWORD` | Senha do administrador inicial, com exatamente 6 caracteres (mesma regra do login). |
| `API_KEY_INICIAL` | Chave de API inicial, criada com o plano PRO. Sugestão: um UUID. É o valor que vai no header `X-API-Key`. |

Se não existir nenhum ADMIN no banco, a aplicação cria um com `ADMIN_USERNAME` e `ADMIN_PASSWORD` ao subir. Depois que já existe um ADMIN, essas duas variáveis deixam de ser lidas. Do mesmo jeito, se não existir nenhuma chave de API, a aplicação cria uma com `API_KEY_INICIAL`. Depois disso, a variável deixa de ser lida.

> **A aplicação falha de propósito ao subir** quando falta `JWT_SECRET`, quando não há ADMIN e as credenciais dele não foram informadas, ou quando não há chave de API e `API_KEY_INICIAL` não foi informada. A mensagem de erro no console diz qual variável falta.

```bash
git clone https://github.com/fidelisspp/Estacionamento-API.git
cd Estacionamento-API
export JWT_SECRET="troque-por-um-segredo-longo-e-aleatorio"
export ADMIN_USERNAME="admin@email.com"
export ADMIN_PASSWORD="Adm#26"
export API_KEY_INICIAL="3f2b8c1e-4a5d-4e6f-9a7b-1c2d3e4f5a6b"
./mvnw spring-boot:run
```

No Windows (PowerShell):
```powershell
$env:JWT_SECRET = "troque-por-um-segredo-longo-e-aleatorio"
$env:ADMIN_USERNAME = "admin@email.com"
$env:ADMIN_PASSWORD = "Adm#26"
$env:API_KEY_INICIAL = "3f2b8c1e-4a5d-4e6f-9a7b-1c2d3e4f5a6b"
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. As tabelas são criadas automaticamente na primeira execução.

| Recurso | Endereço |
|---|---|
| Swagger UI (documentação interativa) | http://localhost:8080/docs-park.html |
| Especificação OpenAPI (JSON) | http://localhost:8080/docs-park |

O console web do H2 fica desativado.

## Primeiros passos (autenticação)

A API tem duas camadas de acesso:

1. **Chave de API (`X-API-Key`)**: identifica o aplicativo. É exigida em **todas** as rotas `/api/**`, inclusive o cadastro e o login.
2. **JWT (`Authorization: Bearer <token>`)**: identifica o usuário. Só o cadastro de usuário (`POST /api/v1/usuarios`), o login (`POST /api/v1/auth`), a tabela de planos (`GET /api/v1/planos`) e a documentação dispensam o token.

Existem dois perfis:
- **CLIENTE**: perfil de quem se cadastra pela API. Gerencia o próprio cadastro e consulta os próprios estacionamentos.
- **ADMIN**: o atendente do estacionamento. Gerencia vagas, serviços e clientes, e faz check-in e check-out.

**1. Criar um usuário** (nasce sempre como CLIENTE):
```http
POST /api/v1/usuarios
{ "username": "ana@email.com", "password": "123456" }
```

**2. Acesso como ADMIN.** O administrador inicial é criado ao subir a aplicação, com `ADMIN_USERNAME` e `ADMIN_PASSWORD` (veja [Como executar](#como-executar)). Faça login com essas credenciais para usar os recursos de ADMIN.

**3. Fazer login e copiar o token:**
```http
POST /api/v1/auth
{ "username": "ana@email.com", "password": "123456" }
→ { "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

No Swagger, clique em **Authorize**, preencha o `apiKey` com a chave de API e o `security` com o token. A partir daí, todas as requisições enviam os dois.

> O token expira em **20 minutos** (configuração em `JwtUtils.EXPIRE_MINUTES`). Ao receber `401`, faça login de novo.

## Headers

| Header | Onde | Para quê |
|---|---|---|
| `X-API-Key` | Todas as rotas `/api/**` | Chave do aplicativo. Ausente, inválida ou desativada: `401`. |
| `Authorization: Bearer <token>` | Rotas protegidas | Token JWT do usuário, obtido no login. Válido por 20 minutos. |
| `X-Idempotency-Key` | Todo `POST` em `/api/**`, exceto `POST /api/v1/auth` | UUID único por operação. Detalhes em [Idempotência](#idempotência). |
| `X-API-Version` | Opcional, em qualquer rota | Versão do recurso: `1` (padrão) ou `2`. Detalhes em [Versionamento](#versionamento). |

**Headers de resposta:**

| Header | Quando aparece |
|---|---|
| `X-Rate-Limit-Limit` | Em toda resposta de `/api/**`: limite de requisições por minuto do plano da chave. |
| `X-Rate-Limit-Remaining` | Em toda resposta de `/api/**`: requisições que ainda restam no minuto. |
| `Retry-After` | No `429`: segundos até a próxima requisição ser aceita. |
| `X-Idempotency-Replayed: true` | Quando a resposta foi devolvida da idempotência, sem processar de novo. |
| `Location` | No `201`: endereço do recurso criado. |

## Planos e rate limiting

Cada chave de API tem um plano, que define quantas requisições ela pode fazer por minuto (algoritmo token bucket):

| Plano | Requisições por minuto | Preço mensal | Para quem |
|---|---|---|---|
| FREE | 10 | R$ 0,00 | Testes e desenvolvimento |
| BASIC | 60 | R$ 60,00 | Estacionamento pequeno, com uma unidade |
| PRO | 150 | R$ 150,00 | Redes com várias unidades e integrações |

Ao passar do limite, a API responde `429 Too Many Requests`, com o header `Retry-After`. A chave inicial (`API_KEY_INICIAL`) nasce com o plano PRO. O ADMIN pode gerar novas chaves, trocar o plano e desativar chaves em `/api/v1/api-keys`.

## Idempotência

Todo `POST` em `/api/**` (exceto o login) exige o header `X-Idempotency-Key` com um UUID. Ele protege contra cliques duplos e reenvios por falha de rede: o mesmo check-in não é registrado duas vezes.

| Situação | Resposta |
|---|---|
| Header ausente ou que não é um UUID | `400` |
| Chave nova | Processa normalmente e guarda a resposta de sucesso |
| Mesma chave, mesmo endpoint, mesmo corpo e mesmo usuário | Devolve a resposta original, com `X-Idempotency-Replayed: true`, sem processar de novo |
| Mesma chave com outro endpoint, corpo ou usuário | `422` |
| Mesma chave com a primeira requisição ainda em processamento | `409` |

As chaves valem por **24 horas** e são separadas por chave de API. Respostas de erro não ficam guardadas: depois de um erro, dá para reenviar com a mesma chave.

## Versionamento

A versão é escolhida pelo header `X-API-Version`. Sem o header, vale a versão `1`, então nada que já existia quebra. Versão não suportada (ex.: `3`) responde `400`.

Hoje só `GET /api/v1/planos` tem duas versões. As outras rotas respondem igual em qualquer versão suportada.

```http
GET /api/v1/planos                      → v1: { "nome", "limitePorMinuto", "precoMensal", "descricao" }
GET /api/v1/planos  X-API-Version: 2    → v2: { "nome", "descricao", "limites": { "porMinuto", "porHora" },
                                                 "preco": { "valor", "moeda", "formatado": "R$ 60,00" } }
```

## Coleção do Postman

O arquivo [`postman/Estacionamento-API.postman_collection.json`](postman/Estacionamento-API.postman_collection.json) tem todos os endpoints, organizados em pastas na ordem de um roteiro completo: usuários → autenticação → clientes → vagas → serviços → estacionamentos → exclusões → planos e chaves de API → idempotência.

- Importe no Postman com **Import → arquivo**.
- Preencha a variável `apiKey` da coleção com a sua chave de API. Um script da coleção envia o header `X-API-Key` em todas as requisições.
- Todo `POST` sem `X-Idempotency-Key` recebe um UUID novo automaticamente. A pasta **9. Idempotência** usa uma chave fixa: envie duas vezes para ver o `X-Idempotency-Replayed: true` na segunda.
- Para usar a API do Render, troque a variável `baseUrl` para `https://estacionamento-api-6yn3.onrender.com/api/v1`.
- Os logins salvam o token sozinhos, e a coleção inteira usa `Bearer {{token}}`.
- O cadastro de usuário, o cliente, o serviço e o check-in salvam `usuarioId`, `clienteId`, `servicoId` e `recibo`, usados pelas requisições seguintes.
- A descrição de cada requisição diz qual perfil (ADMIN ou CLIENTE) ela exige.

## Regras de negócio

**Cobrança no check-out:**

| Tempo de permanência | Valor |
|---|---|
| Até 15 minutos | R$ 5,00 |
| Até 60 minutos | R$ 9,25 |
| Acima de 60 minutos | R$ 9,25 + R$ 1,75 a cada 15 minutos adicionais |

- O preço dos serviços extras contratados é somado ao valor do tempo.
- A cada **10 estacionamentos concluídos**, o cliente ganha **30% de desconto**.
- O check-in ocupa automaticamente a primeira vaga livre, e o check-out a libera.

**Proteções de integridade** (respondem `409 Conflict`):
- Uma vaga com veículo estacionado não pode ser alterada. Uma vaga já usada em um estacionamento não pode ser excluída.
- Um serviço já usado em um estacionamento não pode ser excluído.
- Um cliente com histórico de estacionamentos não pode ser excluído.
- Um usuário com cadastro de cliente não pode ser excluído, e o ADMIN não pode excluir a própria conta.
- Só estacionamentos **finalizados** podem ser excluídos.

## Endpoints

Todas as rotas começam com `/api/v1`. As listagens são paginadas com `?page=0&size=20&sort=campo,asc`.

### Usuários — `/usuarios`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/usuarios` | Cria um usuário (perfil CLIENTE) | Público |
| GET | `/usuarios/{id}` | Busca um usuário | ADMIN ou o próprio usuário |
| PATCH | `/usuarios/{id}` | Altera a própria senha | O próprio usuário |
| GET | `/usuarios` | Lista os usuários (paginado) | ADMIN |
| GET | `/usuarios/role/{role}` | **Consulta personalizada:** lista por perfil (`ADMIN` ou `CLIENTE`) | ADMIN |
| DELETE | `/usuarios/{id}` | Exclui um usuário | ADMIN |

### Autenticação — `/auth`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth` | Faz login e devolve o token JWT | Público |

### Clientes — `/clientes`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/clientes` | Cadastra o cliente do usuário logado | CLIENTE |
| GET | `/clientes/{id}` | Busca um cliente | ADMIN |
| GET | `/clientes` | Lista os clientes (paginado) | ADMIN |
| GET | `/clientes/detalhes` | Dados do cliente logado | CLIENTE |
| GET | `/clientes/cpf/{cpf}` | **Consulta personalizada:** busca pelo CPF | ADMIN |
| PUT | `/clientes/{id}` | Atualiza nome e CPF | ADMIN |
| DELETE | `/clientes/{id}` | Exclui um cliente sem histórico | ADMIN |

### Vagas — `/vagas`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/vagas` | Cria uma vaga | ADMIN |
| GET | `/vagas/{codigo}` | Busca uma vaga pelo código | ADMIN |
| GET | `/vagas` | Lista as vagas (paginado) | ADMIN |
| GET | `/vagas/status/{status}` | **Consulta personalizada:** lista por status (`LIVRE` ou `OCUPADA`) | ADMIN |
| PUT | `/vagas/{codigo}` | Atualiza código e status | ADMIN |
| DELETE | `/vagas/{codigo}` | Exclui uma vaga nunca usada | ADMIN |

### Serviços — `/servicos`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/servicos` | Cria um serviço extra | ADMIN |
| GET | `/servicos/{id}` | Busca um serviço | ADMIN, CLIENTE |
| GET | `/servicos` | Lista os serviços (paginado) | ADMIN, CLIENTE |
| GET | `/servicos/tipo/{tipo}` | **Consulta personalizada:** lista por tipo | ADMIN, CLIENTE |
| PUT | `/servicos/{id}` | Atualiza um serviço | ADMIN |
| DELETE | `/servicos/{id}` | Exclui um serviço nunca usado | ADMIN |

### Estacionamentos — `/estacionamentos`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/estacionamentos/check-in` | Entrada do veículo (com serviços extras opcionais) | ADMIN |
| GET | `/estacionamentos/{recibo}` | Busca um estacionamento (em aberto ou finalizado) | ADMIN ou o dono do recibo |
| GET | `/estacionamentos/check-in/{recibo}` | Busca um check-in em aberto | ADMIN ou o dono do recibo |
| PUT | `/estacionamentos/check-out/{recibo}` | Saída do veículo e cálculo do valor | ADMIN |
| GET | `/estacionamentos/cpf/{cpf}` | **Consulta personalizada:** histórico de um cliente pelo CPF | ADMIN |
| GET | `/estacionamentos` | Histórico do cliente logado | CLIENTE |
| DELETE | `/estacionamentos/{recibo}` | Exclui um estacionamento finalizado | ADMIN |

### Planos — `/planos`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| GET | `/planos` | Tabela de planos. Versões 1 e 2 pelo header `X-API-Version` | Só `X-API-Key` |

### Chaves de API — `/api-keys`
| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/api-keys` | Gera uma chave para um aplicativo, com o plano escolhido | ADMIN |
| GET | `/api-keys/{id}` | Busca uma chave | ADMIN |
| GET | `/api-keys` | Lista as chaves (paginado) | ADMIN |
| PATCH | `/api-keys/{id}/plano` | Troca o plano de uma chave | ADMIN |
| PATCH | `/api-keys/{id}/desativar` | Desativa uma chave (a última ativa não pode ser desativada) | ADMIN |

**Códigos de status usados:** `200` OK - `201` Created (com cabeçalho `Location`) - `204` No Content - `400` parâmetro inválido, `X-Idempotency-Key` ausente ou versão não suportada - `401` sem chave de API, chave inválida, sem token ou token expirado - `403` sem permissão - `404` não encontrado - `409` conflito com regra de negócio - `422` dados de entrada inválidos ou `X-Idempotency-Key` reutilizada - `429` limite do plano excedido.

## HATEOAS

As respostas seguem o formato **HAL**: cada recurso traz um bloco `_links` com as ações possíveis, e as listagens trazem `_embedded`, `_links` (`first`, `prev`, `next`, `last`) e `page`.

Os links são **condicionais**: cada resposta só mostra as ações que quem consulta consegue executar naquele momento. Exemplo de um estacionamento em aberto, visto pelo ADMIN:

```json
{
  "placa": "ABC-1234",
  "recibo": "20261003-143512",
  "dataEntrada": "2026-10-03 14:35:12",
  "vagaCodigo": "A-01",
  "_links": {
    "self":            { "href": "http://localhost:8080/api/v1/estacionamentos/20261003-143512" },
    "check-out":       { "href": "http://localhost:8080/api/v1/estacionamentos/check-out/20261003-143512" },
    "cliente":         { "href": "http://localhost:8080/api/v1/clientes/cpf/79074426050" },
    "vaga":            { "href": "http://localhost:8080/api/v1/vagas/A-01" },
    "estacionamentos": { "href": "http://localhost:8080/api/v1/estacionamentos/cpf/79074426050{?page,size,sort}", "templated": true }
  }
}
```

- **Pelo estado:** depois do check-out, o link `check-out` some e aparece o `delete`.
- **Pelo perfil:** o CLIENTE não recebe links de ações de ADMIN (`check-out`, `delete`, `vaga`), que responderiam `403`.
- **Navegação entre recursos:** do estacionamento para o cliente e a vaga, e do cliente para o histórico de estacionamentos.

## Testes

O projeto tem **107 testes de integração** que sobem a aplicação e fazem requisições HTTP reais, cobrindo os casos de sucesso, os erros de validação, as permissões e os links HATEOAS de todos os recursos.

```bash
./mvnw test -Dtest='*IT'
```

> As classes de teste terminam em `IT`, e por isso o `./mvnw test` sem o filtro não as executa.

## Estrutura do projeto

```
src/main/java/com/joaovitor/estacionamento_api
├── apikey/            Filtro da chave de API e rate limiting
├── config/            Segurança (Spring Security), CORS, OpenAPI e fuso horário
├── entity/            Entidades JPA
├── exception/         Exceções de regra de negócio
├── idempotencia/      Filtro de idempotência dos POSTs
├── jwt/               Geração e validação do token JWT
├── repository/        Repositórios Spring Data (+ projections)
├── service/           Regras de negócio
├── util/              Cálculo de valores e geração de recibo
└── web/
    ├── controller/    Endpoints REST (HATEOAS + documentação)
    ├── dto/           Objetos de entrada e saída (+ mappers)
    └── exception/     Tratamento global de erros (ErrorMessage)
```
