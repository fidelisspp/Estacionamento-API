# Termos de Uso — Estacionamento API

**Versão dos termos:** 1.0
**Vigência:** a partir de 14/11/2026
**Versão da API a que se aplicam:** 2.1.0 e posteriores

> **Aviso:** a Estacionamento API é um projeto acadêmico, desenvolvido como Projeto Final da disciplina de Desenvolvimento de APIs com Spring Boot. Os planos, preços e condições descritos aqui são fictícios e servem para simular um serviço real. Nenhuma cobrança é feita.

## 1. Aceitação

Ao obter uma chave de API ou enviar qualquer requisição para a Estacionamento API ("API"), você ("Cliente") declara que leu e concorda com estes Termos de Uso. Se não concordar, não utilize a API.

## 2. Descrição do serviço

A API oferece recursos para a gestão de estacionamentos de veículos: cadastro de usuários e clientes, controle de vagas, registro de entrada (check-in) e saída (check-out) de veículos, serviços adicionais e consulta de histórico.

A API é acessada por HTTP, nos endereços informados no [README](README.md), e documentada no Swagger em `/docs-park.html`.

## 3. Chave de API

**3.1.** Todo acesso às rotas `/api/**` exige uma chave de API válida, enviada no header `X-API-Key`.

**3.2.** As chaves são criadas e gerenciadas pelo administrador da API. Cada chave está vinculada a um plano (FREE, BASIC ou PRO).

**3.3.** A chave é pessoal e intransferível. O Cliente é responsável por todo uso feito com a sua chave e deve mantê-la em sigilo. Não a publique em repositórios públicos nem em código executado no navegador, exceto chaves criadas especificamente para esse fim, no plano FREE.

**3.4.** Se a chave for exposta, o Cliente deve avisar o administrador, que poderá desativá-la e emitir uma nova.

**3.5.** Algumas rotas exigem também um token de autenticação (Bearer Token, JWT), obtido em `POST /api/v1/auth`. A chave de API identifica o sistema cliente. O token identifica o usuário dentro desse sistema.

## 4. Planos e preços

| Plano | Requisições por minuto | Preço mensal (fictício) | Indicado para |
|---|---|---|---|
| FREE | 10 | R$ 0,00 | Testes e desenvolvimento |
| BASIC | 60 | R$ 60,00 | Estacionamento pequeno, com uma unidade |
| PRO | 150 | R$ 150,00 | Redes com várias unidades e integrações |

**4.1.** A tabela atualizada de planos pode ser consultada a qualquer momento em `GET /api/v1/planos`.

**4.2.** A troca de plano é feita pelo administrador e vale imediatamente.

**4.3.** Os preços podem ser alterados com aviso prévio, conforme a seção 9.

## 5. Limites de uso (rate limit)

**5.1.** Cada chave tem um limite de requisições por minuto, de acordo com o seu plano. O controle usa o algoritmo *token bucket*: as requisições disponíveis são repostas continuamente ao longo do minuto.

**5.2.** Toda resposta informa o limite e o saldo disponível nos headers `X-Rate-Limit-Limit` e `X-Rate-Limit-Remaining`.

**5.3.** Quando o limite é excedido, a API responde `429 Too Many Requests`, com o header `Retry-After` indicando em quantos segundos o Cliente pode tentar de novo. O Cliente deve respeitar esse tempo antes de repetir a requisição.

## 6. Uso aceitável

É proibido:

- criar ou usar várias chaves para burlar os limites do plano contratado;
- compartilhar, revender ou sublicenciar uma chave;
- enviar requisições automatizadas em volume com o objetivo de sobrecarregar ou derrubar a API;
- tentar acessar dados de outros clientes ou rotas sem autorização;
- explorar falhas de segurança em vez de reportá-las;
- cadastrar dados pessoais de terceiros sem autorização deles.

O descumprimento pode levar à desativação da chave, conforme a seção 8.

## 7. Dados pessoais (LGPD)

**7.1.** A API armazena dados pessoais necessários ao serviço: nome e CPF de clientes, e-mail de usuários, e placa, marca, modelo e cor de veículos. As senhas são guardadas apenas como hash (BCrypt), nunca em texto puro.

**7.2.** Esses dados são tratados exclusivamente para a gestão do estacionamento, com base na execução de contrato, conforme a Lei Geral de Proteção de Dados (Lei nº 13.709/2018).

**7.3.** Para garantir a idempotência, a API guarda por até 24 horas a resposta de cada operação `POST` concluída, associada à chave `X-Idempotency-Key`. Depois desse prazo, o registro é descartado.

**7.4.** Os registros de log da API não exibem o valor das chaves de API.

**7.5.** Ambiente de demonstração: na instância hospedada no Render, os dados são apagados a cada nova publicação da API. Não use dados pessoais reais nesse ambiente.

## 8. Suspensão e desativação

**8.1.** O administrador pode desativar uma chave a qualquer momento em caso de violação destes termos, exposição da chave ou pedido do Cliente.

**8.2.** Uma chave desativada passa a receber `401 Unauthorized` em todas as rotas protegidas.

## 9. Versões e alterações

**9.1.** A API segue o [Versionamento Semântico](https://semver.org/lang/pt-BR/). Toda mudança é registrada no [CHANGELOG](CHANGELOG.md).

**9.2.** Mudanças que quebram a compatibilidade com clientes existentes são publicadas como uma nova versão principal (por exemplo, 2.0.0) e indicadas como **BREAKING** no CHANGELOG.

**9.3.** Quando um recurso ganha um novo formato, a versão anterior continua disponível pelo header `X-API-Version`, para que o Cliente possa migrar no seu ritmo. Hoje, `GET /api/v1/planos` está disponível nas versões `1` (padrão) e `2`.

**9.4.** Estes Termos podem ser atualizados. A versão em vigor é sempre a publicada neste arquivo, com a data de vigência no topo.

## 10. Disponibilidade e limitação de responsabilidade

**10.1.** A API é fornecida "no estado em que se encontra", sem garantia de disponibilidade (SLA). A instância de demonstração usa o plano gratuito do Render, que desliga o serviço quando ele fica parado. A primeira requisição depois disso pode levar alguns minutos.

**10.2.** A API não se responsabiliza por perdas decorrentes de indisponibilidade, perda de dados ou uso indevido da chave pelo Cliente.

## 11. Contato

Dúvidas, pedidos de chave e relatos de falhas de segurança: abra uma issue no repositório [fidelisspp/Estacionamento-API](https://github.com/fidelisspp/Estacionamento-API/issues).
