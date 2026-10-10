# Oficina Mecânica — Front-end

SPA em **React + TypeScript** que consome a API do back-end (`../`). Tem duas áreas:

| Área | Rota | Acesso |
|---|---|---|
| **Oficina** | `/`, `/ordens-servico`, `/clientes`, `/veiculos`, `/servicos`, `/estoque` | login JWT (`/login`) |
| **Cliente** | `/acompanhamento` | pública: o cliente informa o CPF/CNPJ, acompanha a OS e aprova ou rejeita o orçamento |

## Como executar

**Com Docker (recomendado):** na raiz do repositório, `docker compose up -d --build` e acesse **http://localhost:3000**.
O Nginx do container serve o SPA e encaminha `/api` ao back-end.

**Em desenvolvimento** (com a API rodando em `localhost:8080`):

```bash
cd frontend
npm install
npm run dev            # http://localhost:5173 (o Vite encaminha /api para a 8080)
```

Para apontar o proxy para outra API: `VITE_API_PROXY=http://outro-host:8080 npm run dev`.

| Script | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento com HMR |
| `npm run build` | Checagem de tipos + build de produção em `dist/` |
| `npm test` / `npm run test:watch` | Testes (Vitest + Testing Library) |
| `npm run test:coverage` | Testes com relatório de cobertura em `coverage/` |
| `npm run lint` / `npm run format` | Lint (oxlint) / formatação (Prettier) |

## Tecnologias

| Necessidade | Escolha | Por quê |
|---|---|---|
| Build | **Vite** | Dev server instantâneo, build otimizado com code splitting |
| UI | **React 19 + TypeScript (strict)** | Tipos espelham os DTOs da API; erros de contrato aparecem na compilação |
| Rotas | **React Router** (data router) | Rotas protegidas, carregamento sob demanda (`lazy`) por página |
| Estado do servidor | **TanStack Query** | Cache, revalidação, estados de carregamento/erro e invalidação após mutações; dispensa store global |
| Formulários | **React Hook Form + Zod** | Validação declarativa com tipos inferidos; erros por campo da API (`campos` do problem+json) vão para o campo certo |
| Estilo | **Tailwind CSS** | Sem CSS morto; tokens de tema em `src/index.css` |
| Testes | **Vitest + Testing Library** | Testam o comportamento como o usuário vê (papéis e rótulos acessíveis) |

## Arquitetura

A organização segue a mesma ideia do back-end (DDD + Clean Architecture): **módulos por contexto de negócio** e
regras de domínio **isoladas da UI e do HTTP**.

```
src/
├── app/                    # Composição: rotas, layouts, providers (QueryClient, notificações, confirmação)
├── modules/                # Um módulo por contexto do domínio
│   ├── autenticacao/       #   sessão (token + expiração), login, rota protegida
│   ├── ordens-servico/     #   núcleo: lista, abertura, detalhe e fluxo da OS
│   │   ├── dominio/        #     regras puras: transições de status, ações permitidas, linha do tempo
│   │   ├── componentes/    #     peças reutilizadas também pela área do cliente
│   │   ├── api.ts          #     adaptador HTTP (endpoints)
│   │   ├── hooks.ts        #     cache (TanStack Query)
│   │   └── tipos.ts        #     modelo de leitura (espelha OrdemServicoResponse)
│   ├── acompanhamento/     #   área pública do cliente
│   ├── clientes/ veiculos/ servicos/ estoque/   # cadastros
│   └── painel/             #   visão geral + tempo médio de execução
├── shared/                 # Sem dependência de nenhum módulo
│   ├── api/                #   cliente HTTP (RFC 9457 → ApiError) e fábrica de CRUD
│   ├── dominio/            #   objetos de valor: Documento (CPF/CNPJ), Placa, formatos
│   ├── forms/ hooks/       #   utilitários de formulário e listas
│   └── ui/                 #   design system (Botão, Campo, Diálogo, Cartão, Selo…)
└── test/                   # setup e helpers de teste
```

Decisões relevantes:

- **Dependências apontam para dentro.** `shared/` não importa módulos; o cliente HTTP recebe o token e a reação ao
  401 por `configurarHttp` (inversão de dependência), ligados em `main.tsx`.
- **Regras de domínio replicadas, não reinventadas.** `dominio/status.ts` espelha `StatusOrdemServico` e
  `documento.ts` espelha `Documento` (inclusive CNPJ alfanumérico). Os testes usam **os mesmos casos** dos testes Java.
  A UI só oferece ações válidas, mas **o back-end continua sendo a fonte da verdade**.
- **Toda operação da OS devolve a OS atualizada:** o cache do detalhe é atualizado sem nova requisição; listas,
  relatório e estoque são invalidados (a aprovação baixa o estoque).
- **Mesma origem para a API** (proxy do Vite em dev, Nginx em produção): sem CORS no back-end e sem a porta da API exposta ao navegador.

## Segurança

- Token em `sessionStorage`, descartado ao expirar (`expiraEmSegundos`) ou ao receber 401; o cache é limpo no logout.
  *Evolução recomendada:* emitir o JWT em cookie `HttpOnly` + `SameSite=Strict`, inacessível a JavaScript.
- Redirecionamento pós-login aceita só caminhos internos (evita *open redirect*).
- O CPF/CNPJ da área do cliente fica só em memória: não vai para a URL nem para o storage (LGPD).
- Nginx sem root, com CSP restritiva, `X-Frame-Options`, `nosniff`, `Referrer-Policy` e `Permissions-Policy`.
- React escapa o conteúdo renderizado; não há `dangerouslySetInnerHTML`.

## Acessibilidade

Rótulos associados a todos os campos, erros ligados por `aria-describedby`, diálogos com `<dialog>` nativo (foco
preso, `Esc`), estados `aria-pressed`/`aria-current`, link “pular para o conteúdo” e layout responsivo.
