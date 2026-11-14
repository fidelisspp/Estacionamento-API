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

## [2.1.0] - 2026-11-14

### Added

- Termos de Uso (`TERMOS-DE-USO.md`), com link "Terms of service" no Swagger.
- Front-end de vitrine (`frontend/index.html`), em HTML e JavaScript puro, que consome `GET /api/v1/planos` com `fetch` e uma chave de API do plano FREE própria para o navegador. Mostra os planos em cards, alterna entre as versões 1 e 2 da API pelo header `X-API-Version` e exibe as requisições restantes do plano, com contagem regressiva pelo `Retry-After` quando o limite é atingido.

### Fixed

- CORS passa a expor os headers de rate limit (`Retry-After`, `X-Rate-Limit-Limit`, `X-Rate-Limit-Remaining`) para clientes web.
- Os registros de idempotência com mais de 24 horas passam a ser apagados por uma tarefa agendada, de hora em hora. Antes, um registro vencido só era apagado quando a mesma `X-Idempotency-Key` era reenviada, e a resposta guardada ficava no banco indefinidamente.

## [2.0.0] - 2026-11-14

### Added

- Esquema de segurança `apiKey` (header `X-API-Key`) na documentação Swagger, exigido em todas as rotas `/api/**`, e resposta `401` documentada nessas rotas.
- Rate limiting por chave de API com o algoritmo token bucket (Bucket4j), conforme o plano: FREE 10, BASIC 60 e PRO 150 requisições por minuto. Toda resposta traz `X-Rate-Limit-Limit` e `X-Rate-Limit-Remaining`. Ao exceder o limite, a API responde `429 Too Many Requests` com o header `Retry-After` e o tempo de espera na mensagem.
- Resposta `429` documentada no Swagger em todas as rotas `/api/**`, com os headers `Retry-After`, `X-Rate-Limit-Limit` e `X-Rate-Limit-Remaining`.
- Idempotência em todas as requisições `POST` de `/api/**` (exceto o login) pelo header `X-Idempotency-Key` (UUID): o reenvio da mesma chave devolve a resposta original, sem processar de novo, com o header `X-Idempotency-Replayed: true`. Chave reutilizada com outro endpoint, corpo ou usuário responde `422`; requisição com a mesma chave ainda em processamento responde `409`. As chaves valem por 24 horas, e só respostas de sucesso ficam guardadas.
- Versionamento de API pelo header `X-API-Version` (suporte nativo do Spring Framework 7). Versões suportadas: `1` (padrão, quando o header não é enviado) e `2`. `GET /api/v1/planos` ganhou a versão 2, que agrupa os limites (por minuto e por hora) e o preço (valor, moeda e valor formatado em reais). Versão não suportada responde `400`; endpoints sem versão declarada respondem a qualquer versão suportada.

### Changed

- **BREAKING:** todas as rotas em `/api/**` passam a exigir o header `X-API-Key` com uma chave ativa. Requisições sem chave, ou com chave inválida ou inativa, recebem `401 Unauthorized`.
- **BREAKING:** requisições `POST` em `/api/**` (exceto `POST /api/v1/auth`) passam a exigir o header `X-Idempotency-Key` com um UUID. Sem ele, a API responde `400`.

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

[Unreleased]: https://github.com/fidelisspp/Estacionamento-API/compare/v2.1.0...HEAD
[2.1.0]: https://github.com/fidelisspp/Estacionamento-API/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/fidelisspp/Estacionamento-API/compare/v1.1.0...v2.0.0
[1.1.0]: https://github.com/fidelisspp/Estacionamento-API/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/fidelisspp/Estacionamento-API/releases/tag/v1.0.0
