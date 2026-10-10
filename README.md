# Oficina Mecânica — Sistema Integrado de Atendimento e Execução de Serviços

MVP do back-end de uma oficina mecânica de médio porte, desenvolvido para o **Tech Challenge — Fase 1** da
pós-graduação **SOAT — Software Architecture**.

| | |
|---|---|
| **Vídeo da entrega** | _⟨link do YouTube⟩_ |
| **Documentação DDD (Miro)** | _⟨link do board⟩_ — cópia exportada em [`docs/ddd/`](docs/ddd) |
| **Swagger (local)** | http://localhost:8080/swagger-ui.html |
| **Front-end (local)** | http://localhost:3000 — detalhes em [`frontend/README.md`](frontend/README.md) |

---

## Sumário

1. [Objetivo](#1-objetivo)
2. [Descrição da solução](#2-descrição-da-solução)
3. [Arquitetura](#3-arquitetura)
4. [Tecnologias](#4-tecnologias)
5. [Banco de dados: decisão e justificativa](#5-banco-de-dados-decisão-e-justificativa)
6. [Pré-requisitos](#6-pré-requisitos)
7. [Como executar](#7-como-executar)
8. [Autenticação e credenciais de demonstração](#8-autenticação-e-credenciais-de-demonstração)
9. [Documentação da API (Swagger)](#9-documentação-da-api-swagger)
10. [Testes e cobertura](#10-testes-e-cobertura)
11. [Análise de vulnerabilidades](#11-análise-de-vulnerabilidades)
12. [Estrutura de diretórios](#12-estrutura-de-diretórios)
13. [Documentação DDD](#13-documentação-ddd)

---

## 1. Objetivo

A oficina controlava atendimento, diagnóstico, execução e entrega dos veículos com anotações manuais e
planilhas, o que causava erros de priorização, falhas no controle de peças, perda de histórico e um fluxo de
orçamento e autorização ineficiente.

Este projeto entrega a primeira versão do back-end que organiza esse processo: **gestão de ordens de serviço,
clientes, veículos, serviços, peças e estoque**, com o cliente acompanhando e aprovando o orçamento via API.

## 2. Descrição da solução

- **Criação da Ordem de Serviço (OS):** o cliente é identificado por **CPF ou CNPJ** (validados, inclusive o
  CNPJ alfanumérico); o veículo é localizado pela placa ou cadastrado na hora (placa, marca, modelo, ano);
  serviços e peças/insumos são incluídos e o **orçamento é calculado automaticamente**.
- **Orçamento:** a oficina envia o orçamento; o **cliente consulta e aprova/rejeita pela API pública**
  (ou a oficina registra a resposta recebida por telefone).
- **Acompanhamento:** status `RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → EM_EXECUCAO → FINALIZADA → ENTREGUE`,
  alterados **automaticamente** pelas ações do sistema e protegidos por regras de transição; o histórico de
  cada mudança fica registrado.
- **Estoque:** a aprovação do orçamento **baixa o estoque na mesma transação** (se faltar uma peça, nada é
  gravado); entradas e saídas manuais; alerta de estoque abaixo do mínimo.
- **Gestão:** CRUD de clientes, veículos, serviços e peças/insumos; listagem e detalhamento de OS;
  **tempo médio de execução** geral e por serviço.
- **Segurança:** APIs administrativas protegidas por **JWT**; validação de CPF/CNPJ e placa (antiga e Mercosul).

## 3. Arquitetura

**Monólito em camadas seguindo Clean Architecture + Domain-Driven Design.** As dependências apontam sempre
para dentro: o domínio não conhece Spring, JPA nem HTTP.

```
          ┌───────────────────────────── Adaptadores ──────────────────────────────┐
          │  interfaces.rest                         infrastructure                 │
          │  Controllers, DTOs, erros RFC 9457,      JPA (entidades + adapters),    │
          │  OpenAPI                                 Flyway, Spring Security/JWT,   │
          │                                          eventos, wiring, transações    │
          └──────────────────────┬──────────────────────────────┬──────────────────┘
                                 ▼                              ▼
          ┌──────────────────────────── application ───────────────────────────────┐
          │  Casos de uso: AbrirOrdemServico, GerenciarItens, AcompanharOrdemServico,│
          │  ResponderOrcamento, ConsultarOrdensServico, CalcularTempoMedio, CRUDs   │
          │  Portas de saída: PublicadorEventos, AutenticacaoPort                    │
          └──────────────────────────────────┬─────────────────────────────────────┘
                                             ▼
          ┌────────────────────────────── domain (Java puro) ──────────────────────┐
          │  Agregados: OrdemServico, Cliente, Veiculo, Servico, Peca               │
          │  Objetos de valor: Documento, Placa, Dinheiro, Email, Orcamento         │
          │  Eventos de domínio, serviço de domínio, portas de repositório          │
          └────────────────────────────────────────────────────────────────────────┘
```

| Camada | Pacote | Responsabilidade |
|---|---|---|
| Domínio | `br.com.oficina.domain` | Regras de negócio e invariantes (transições de status, cálculo do orçamento, estoque nunca negativo, validação de CPF/CNPJ/placa). Java puro. |
| Aplicação | `br.com.oficina.application` | Casos de uso que orquestram o domínio por meio de portas; cada método é uma unidade de trabalho. |
| Infraestrutura | `br.com.oficina.infrastructure` | Adaptadores das portas: JPA/PostgreSQL, JWT, publicação de eventos, composição dos casos de uso. |
| Interface | `br.com.oficina.interfaces.rest` | API REST, validação de entrada (Bean Validation), mapeamento de erros e documentação OpenAPI. |

Decisões relevantes:

- **Agregados referenciam outros agregados por ID**, e os itens da OS guardam o preço vigente na inclusão
  (alterar o catálogo não muda orçamentos já montados).
- **Transações declarativas sem anotações na aplicação:** `TransacoesConfig` aplica um `TransactionInterceptor`
  a todo método público de classes `*UseCase` (consultas em modo somente leitura). Na aprovação do orçamento, a
  baixa de várias peças e a mudança de status são atômicas.
- **Concorrência:** bloqueio pessimista (`SELECT … FOR UPDATE`) na baixa de estoque, sempre na mesma ordem de
  peças (evita deadlock), e `@Version` (optimistic locking) na OS.
- **Eventos de domínio** são acumulados nos agregados e tratados após o commit (`@TransactionalEventListener`),
  simulando as notificações ao cliente.
- A regra de dependência é **verificada por teste** (`ArquiteturaTest`, com ArchUnit).

Diagramas completos (contextos, agregados, máquina de estados, sequência, modelo de dados) em
[`docs/ddd/diagramas.md`](docs/ddd/diagramas.md).

## 4. Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | **Java 25** |
| Framework | Spring Boot 4.1 (Spring MVC, Spring Data JPA/Hibernate 7, Spring Security 7, Validation, Actuator) |
| Banco de dados | PostgreSQL 17 · migrations com Flyway |
| Segurança | JWT HS256 (OAuth2 Resource Server + Nimbus JOSE), BCrypt |
| Documentação da API | springdoc-openapi 3 (OpenAPI 3.1 + Swagger UI) |
| Testes | JUnit 5, AssertJ, Spring MockMvc, H2 (modo PostgreSQL), ArchUnit, JaCoCo |
| Segurança da cadeia | Trivy (dependências, imagem, segredos, Dockerfile) |
| Build / execução | Maven (wrapper incluso), Docker multi-stage, Docker Compose |
| CI | GitHub Actions (testes + cobertura + Trivy + build da imagem) |

## 5. Banco de dados: decisão e justificativa

**Escolha: PostgreSQL (relacional).**

1. **O domínio é relacional e precisa de integridade.** Cliente → veículos → ordens de serviço → itens, que
   referenciam serviços e peças. Chaves estrangeiras, `UNIQUE` (CPF/CNPJ, placa, código da peça) e
   `CHECK (quantidade_estoque >= 0)` reforçam no banco as invariantes do domínio.
2. **Transações ACID nos fluxos críticos.** Aprovar um orçamento altera a OS e baixa o estoque de várias peças
   ao mesmo tempo: tudo ou nada. Com `SELECT … FOR UPDATE`, duas aprovações simultâneas não consomem o mesmo
   saldo.
3. **Consultas e relatórios.** Listagens por status, histórico, tempo médio de execução e estoque abaixo do
   mínimo são consultas naturais em SQL, com índices simples.
4. **Maduro, gratuito e fácil de operar.** Imagem oficial para Docker, suporte de primeira linha no
   Hibernate/Flyway, tipo `UUID` nativo e `TIMESTAMP WITH TIME ZONE` para o histórico.

Alternativas consideradas: **MongoDB** (o modelo de documento caberia na OS, mas a baixa de estoque atômica
entre documentos e a integridade referencial ficariam na aplicação) e **MySQL** (atenderia, mas o PostgreSQL
tem `UUID` nativo e é o banco usado no restante do curso). Nos testes usamos H2 em modo de compatibilidade
com PostgreSQL, rodando as **mesmas migrations Flyway**.

## 6. Pré-requisitos

- **Para rodar com Docker (recomendado):** Docker 24+ com Docker Compose v2. Nada mais.
- **Para rodar/testar localmente:** JDK 25+ (o Maven vem com o wrapper `./mvnw`).

## 7. Como executar

### Opção A — tudo com Docker Compose (aplicação + PostgreSQL)

```bash
cp .env.example .env        # ajuste as senhas e a chave JWT (veja abaixo)
docker compose up -d --build
```

- Front-end: http://localhost:3000 (área da oficina e acompanhamento do cliente em `/acompanhamento`)
- API: http://localhost:8080 · Swagger: http://localhost:8080/swagger-ui.html · Health: http://localhost:8080/actuator/health
- Para gerar uma chave JWT forte: `openssl rand -base64 48`
- Se a porta 5432 já estiver em uso na sua máquina, altere `DB_PORTA_HOST` no `.env`.
- Para parar: `docker compose down` (os dados ficam no volume `oficina-dados`; `docker compose down -v` apaga tudo).

### Opção B — aplicação local, banco no Docker

```bash
cp .env.example .env
docker compose up -d postgres
set -a && source .env && set +a
export DB_URL=jdbc:postgresql://localhost:${DB_PORTA_HOST:-5432}/oficina
./mvnw spring-boot:run
```

O Flyway cria o esquema e carrega um catálogo inicial (5 serviços e 4 peças/insumos) na primeira execução.

### Opção C — produção na AWS (EC2 + Terraform + GitHub Actions)

Passo a passo completo em [`docs/guias/deploy-aws.md`](docs/guias/deploy-aws.md): o Terraform (`infra/terraform`)
cria a infraestrutura e cada push na `main` que passa no CI é publicado automaticamente numa EC2.

### Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL` | não (padrão `jdbc:postgresql://localhost:5432/oficina`) | URL JDBC |
| `DB_USERNAME` / `DB_PASSWORD` | sim | Credenciais do PostgreSQL |
| `JWT_SECRET` | sim | Chave HMAC do JWT, mínimo 32 caracteres (a aplicação não sobe sem ela) |
| `JWT_EXPIRACAO_MINUTOS` | não (60) | Validade do token |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | sim (senha) | Usuário administrador criado na primeira inicialização |
| `DB_PORTA_HOST` | não (5432) | Porta do PostgreSQL publicada no host |
| `FRONTEND_PORTA_HOST` | não (3000) | Porta do front-end publicada no host |

## 8. Autenticação e credenciais de demonstração

Nenhuma senha fica no código ou nas migrations. O usuário administrador é criado na primeira inicialização com
os valores de `ADMIN_USERNAME` / `ADMIN_PASSWORD` do `.env` (no `.env.example`: usuário **`admin`**, senha
**`troque-esta-senha-admin`**).

1. `POST /api/auth/login` com `{"usuario": "admin", "senha": "<ADMIN_PASSWORD>"}` → devolve `token`.
2. Envie `Authorization: Bearer <token>` nas rotas `/api/admin/**`. No Swagger, clique em **Authorize** e cole o token.

| Rotas | Acesso |
|---|---|
| `POST /api/auth/login` | público |
| `/api/admin/**` (cadastros, OS, estoque, relatórios) | JWT com perfil `ADMIN` |
| `/api/publico/ordens-servico/**` (acompanhamento e aprovação pelo cliente) | público; o cliente se identifica pelo CPF/CNPJ no cabeçalho `X-Documento-Cliente` e só vê as próprias OS |
| Swagger, `/actuator/health` | público |

Um roteiro com o fluxo completo, pronto para executar no IntelliJ, está em
[`docs/api/demo-fluxo-completo.http`](docs/api/demo-fluxo-completo.http).

## 9. Documentação da API (Swagger)

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- OpenAPI JSON: http://localhost:8080/v3/api-docs · cópia versionada em [`docs/api/openapi.json`](docs/api/openapi.json)

Principais endpoints:

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/admin/ordens-servico` | Abre OS (CPF/CNPJ + veículo + serviços/peças) |
| POST | `/api/admin/ordens-servico/{id}/diagnostico` | Inicia o diagnóstico |
| POST/DELETE | `/api/admin/ordens-servico/{id}/servicos` · `/pecas` | Inclui/remove itens (orçamento recalculado) |
| POST | `/api/admin/ordens-servico/{id}/orcamento/envio` | Envia o orçamento ao cliente |
| POST | `/api/admin/ordens-servico/{id}/orcamento/aprovacao` · `/rejeicao` | Oficina registra a resposta |
| POST | `/api/admin/ordens-servico/{id}/finalizacao` · `/entrega` | Finaliza e entrega |
| GET | `/api/admin/ordens-servico?status=` · `/{id}` | Lista e detalha OS |
| GET | `/api/admin/relatorios/tempo-medio-execucao` | Tempo médio de execução |
| GET | `/api/publico/ordens-servico/{id}` | Cliente acompanha a OS |
| POST | `/api/publico/ordens-servico/{id}/orcamento/resposta` | Cliente aprova/rejeita |
| CRUD | `/api/admin/clientes` · `/veiculos` · `/servicos` · `/pecas` | Gestão administrativa |
| POST | `/api/admin/pecas/{id}/estoque/entrada` · `/saida` | Movimentação de estoque |

Erros seguem a RFC 9457 (`application/problem+json`): **400** dados inválidos, **401** não autenticado,
**404** não encontrado, **409** conflito (duplicidade/registro em uso), **422** regra de negócio violada.

## 10. Testes e cobertura

```bash
./mvnw verify
```

Executa **138 testes** e falha o build se a cobertura de linhas ficar abaixo de **80%** nos domínios críticos
(`domain` e `application`). Relatório HTML em `target/site/jacoco/index.html`.

| Tipo | Onde | O que cobre |
|---|---|---|
| Unitários de domínio | `src/test/java/.../domain` | Invariantes dos agregados, transições de status, orçamento, CPF/CNPJ/placa, estoque, tempo médio |
| Unitários de aplicação | `src/test/java/.../application` | Casos de uso com repositórios em memória (sem Spring/banco) |
| Integração | `src/test/java/.../integracao` | API completa (HTTP + segurança + JPA + Flyway em H2): fluxo da OS ponta a ponta, rejeição, **rollback na falta de estoque**, CRUDs, validações, JWT |
| Arquitetura | `src/test/java/.../arquitetura` | Regra de dependência da Clean Architecture (ArchUnit) |

Cobertura de linhas na entrega: **domain 100%**, **application 100%**, projeto todo ~99%.

## 11. Análise de vulnerabilidades

Análise feita com **Trivy** sobre a imagem Docker e o repositório (dependências, segredos e Dockerfile).
As 19 vulnerabilidades encontradas na primeira análise (Tomcat, Jackson, libpng, zlib) foram corrigidas; o
resultado final é **zero vulnerabilidades**. Relatório completo:
[`docs/seguranca/relatorio-vulnerabilidades.md`](docs/seguranca/relatorio-vulnerabilidades.md).
O CI repete a análise a cada push/PR.

## 12. Estrutura de diretórios

```
.
├── src/main/java/br/com/oficina
│   ├── domain/                     # Núcleo (Java puro)
│   │   ├── shared/                 #   AgregadoRaiz, EventoDominio, Dinheiro, Email, exceções
│   │   ├── cliente/                #   Cliente, Documento (CPF/CNPJ), ClienteRepository
│   │   ├── veiculo/                #   Veiculo, Placa, VeiculoRepository
│   │   ├── servico/                #   Servico (catálogo), ServicoRepository
│   │   ├── estoque/                #   Peca, TipoPeca, EstoqueAbaixoDoMinimo, PecaRepository
│   │   └── ordemservico/           #   OrdemServico (raiz), itens, Orcamento, StatusOrdemServico,
│   │       └── evento/             #   eventos de domínio da OS
│   ├── application/                # Casos de uso e portas de saída
│   │   ├── ordemservico/           #   Abrir, GerenciarItens, Acompanhar, ResponderOrcamento, Consultar, TempoMedio
│   │   ├── cliente/ veiculo/ servico/ estoque/ autenticacao/ shared/
│   ├── infrastructure/             # Adaptadores
│   │   ├── persistence/            #   entity/ (JPA), jpa/ (Spring Data), adapter/ (implementa portas do domínio)
│   │   ├── security/               #   SecurityConfig, JWT, usuário admin inicial
│   │   ├── evento/                 #   publicação e tratamento dos eventos de domínio
│   │   └── config/                 #   composição dos casos de uso e transações
│   └── interfaces/rest/            # Controllers, DTOs, tratamento de erros, OpenAPI
├── frontend/                       # SPA React + TypeScript (ver frontend/README.md)
├── infra/terraform/                # Infraestrutura AWS (EC2, ECR, IAM/OIDC, segredos)
├── deploy/                         # Compose de produção e scripts executados na EC2
├── src/main/resources
│   ├── application.yml
│   └── db/migration/               # Flyway: V1 esquema, V2 catálogo inicial
├── src/test/java/br/com/oficina    # domain/, application/, integracao/, arquitetura/
├── docs
│   ├── ddd/                        # event-storming.pdf, diagramas.pdf/.md, linguagem-ubiqua.md, fonte/
│   ├── api/                        # openapi.json, demo-fluxo-completo.http
│   └── seguranca/                  # relatório e saídas do Trivy
├── .github/workflows/ci.yml
├── Dockerfile
├── docker-compose.yml
└── .env.example
```

## 13. Documentação DDD

| Documento | Conteúdo |
|---|---|
| _⟨link do Miro⟩_ | Board com Event Storming e diagramas |
| [`docs/ddd/event-storming.pdf`](docs/ddd/event-storming.pdf) | Event Storming: criação da OS, acompanhamento da OS, gestão de peças e insumos (atores, comandos, agregados, eventos, políticas, modelos de leitura, hotspots) |
| [`docs/ddd/diagramas.pdf`](docs/ddd/diagramas.pdf) / [`.md`](docs/ddd/diagramas.md) | Mapa de contextos, agregados/entidades/objetos de valor, máquina de estados, eventos, arquitetura, sequência da aprovação, modelo de dados, relação documentação ↔ código |
| [`docs/ddd/linguagem-ubiqua.md`](docs/ddd/linguagem-ubiqua.md) | Linguagem ubíqua, glossário com o mapeamento termo → classe e regras de negócio |

Os PDFs são gerados a partir dos fontes em `docs/ddd/fonte/` com `docs/ddd/fonte/gerar_pdfs.sh`.
