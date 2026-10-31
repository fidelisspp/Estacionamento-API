# Changelog

Todas as mudanças relevantes da API de Gestão de Estacionamento são documentadas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto adota o [Versionamento Semântico](https://semver.org/lang/pt-BR/)

## tipos de alterações

Added - novas funcionalidades.
Changed - alterações na funcionalidade existente.
Deprecated - funcionalidades que serão removidas em breve.
Removed - Recursos removidos por enquanto.
Fixed - Correções de erros.
Security - Em caso de vulnerabilidades.

## Como ler as versões

Os números de versão seguem o padrão **MAIOR.MENOR.CORREÇÃO**:

- **MAIOR** (ex.: 2.0.0): mudanças incompatíveis. Clientes da versão anterior precisam se adaptar.
- **MENOR** (ex.: 1.1.0): novas funcionalidades que não quebram quem já usa a API.
- **CORREÇÃO** (ex.: 1.0.1): correções de bugs, sem mudança de comportamento esperado.

## [Unreleased]

## [1.1.0] - 2026-10-30

### Added

- Política de CORS global para `/api/**`, com origens configuráveis pela property `cors.allowed-origins` (padrão: localhost nas portas 3000, 4200 e 5173).
- Deploy em produção no Render via Docker (build multi-stage com Java 17).
- Criação automática do ADMIN inicial ao subir a aplicação, quando não existe nenhum, com as variáveis de ambiente `ADMIN_USERNAME` e `ADMIN_PASSWORD`.

### Security

- O segredo do JWT saiu do código e passou a ser lido da variável de ambiente `JWT_SECRET` (mínimo de 32 caracteres). A aplicação não sobe sem ele. Tokens emitidos antes desta versão deixam de valer.
- O console web do H2 (`/h2-console`) vem desativado por padrão e deixou de ser público.
- O arquivo do banco de dados (`data/`) foi removido do repositório e adicionado ao `.gitignore`.

## [1.0.0] - 2026-10-06

Primeira versão estável da API, entregue como Parte 1 do Projeto Final.

### Added

#### Autenticação e acesso
- Login com e-mail e senha em `POST /api/v1/auth`, retornando um token JWT (Bearer) válido por 20 minutos.
- Controle de acesso por perfil (`ADMIN` e `CLIENTE`) em todos os recursos protegidos.

#### Usuários
- Cadastro público de usuário com e-mail e senha de 6 caracteres, sempre com perfil `CLIENTE`.
- Consulta de usuário por id (pelo `ADMIN` ou pelo próprio usuário).
- Alteração da própria senha, com confirmação da senha atual.
- Listagem paginada de usuários e listagem paginada por perfil (`ADMIN`).
- Exclusão de usuário pelo `ADMIN`, exceto o próprio usuário logado e usuários que já têm cadastro de cliente.

#### Clientes
- Cadastro de cliente (nome e CPF validado) vinculado ao usuário logado.
- Consulta dos dados do próprio cliente logado.
- Consulta de cliente por id e por CPF (`ADMIN`).
- Listagem paginada de clientes (`ADMIN`).
- Atualização de nome e CPF (`ADMIN`).
- Exclusão de cliente sem histórico de estacionamentos (`ADMIN`).

#### Vagas
- Cadastro de vaga com código único de 4 caracteres e status (`LIVRE`/`OCUPADA`).
- Consulta de vaga por código.
- Listagem paginada de vagas e listagem paginada por status.
- Atualização de código e status, bloqueada quando há veículo estacionado.
- Exclusão de vaga que nunca foi usada em um estacionamento.

#### Serviços extras
- Cadastro de serviço extra com nome, descrição, preço e tipo (`LAVAGEM`, `MANOBRISTA`, `CALIBRAGEM`, `OUTRO`) (`ADMIN`).
- Consulta de serviço por id, listagem paginada e listagem paginada por tipo (`ADMIN` e `CLIENTE`).
- Atualização de serviço (`ADMIN`).
- Exclusão de serviço que nunca foi usado em um estacionamento (`ADMIN`).

#### Estacionamentos
- Check-in de veículo com alocação automática da primeira vaga livre, geração de recibo e contratação opcional de serviços extras.
- Check-out com cálculo do valor e liberação da vaga. Tabela de cobrança: até 15 min, R$ 5,00; até 60 min, R$ 9,25; depois, R$ 1,75 a cada 15 min adicionais; somam-se os preços dos serviços extras.
- Desconto de 30% no estacionamento seguinte a cada 10 concluídos pelo cliente (11º, 21º, ...).
- Consulta por recibo (aberto ou finalizado) e consulta apenas de check-ins em aberto. O cliente só acessa os próprios recibos.
- Listagem paginada do histórico de um cliente por CPF (`ADMIN`) e do histórico do cliente logado.
- Exclusão de estacionamento já finalizado (`ADMIN`).

#### Geral
- Respostas no formato HAL com links HATEOAS de navegação e das ações disponíveis para o perfil de quem consulta.
- Respostas de erro padronizadas em JSON (path, método, status, mensagem e erros por campo).
- Documentação interativa OpenAPI/Swagger em `/docs-park.html`.

