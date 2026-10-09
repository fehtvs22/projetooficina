# Diagramas DDD — Oficina Mecânica

Os diagramas abaixo descrevem a solução **como implementada** no código deste repositório.
Versão em PDF: [diagramas.pdf](diagramas.pdf) · Event Storming: [event-storming.pdf](event-storming.pdf) · Glossário: [linguagem-ubiqua.md](linguagem-ubiqua.md)

## 1. Mapa de contextos (Bounded Contexts)

Por ser um MVP monolítico, os contextos são **módulos (pacotes) do mesmo monólito**, com fronteiras explícitas:
os agregados de um contexto referenciam os de outro **apenas por identificador**, nunca por objeto.

```mermaid
flowchart LR
    subgraph CORE["Núcleo"]
        ATD["<b>Atendimento</b><br/>Ordem de Serviço, Orçamento,<br/>Status, Tempo médio<br/><i>domain.ordemservico</i>"]
    end
    subgraph SUP["Suporte"]
        CAD["<b>Cadastro</b><br/>Cliente, Veículo<br/><i>domain.cliente / domain.veiculo</i>"]
        CAT["<b>Catálogo de Serviços</b><br/>Serviço<br/><i>domain.servico</i>"]
        EST["<b>Estoque</b><br/>Peça / Insumo<br/><i>domain.estoque</i>"]
    end
    subgraph GEN["Genérico"]
        IAM["<b>Identidade e Acesso</b><br/>Usuário, JWT<br/><i>infrastructure.security</i>"]
    end
    CAD -- "U → D: clienteId, veiculoId" --> ATD
    CAT -- "U → D: servicoId + preço congelado" --> ATD
    EST -- "U → D: pecaId + preço; disponibilidade" --> ATD
    ATD -- "evento OrcamentoAprovado ⇒ baixa de estoque" --> EST
    IAM -. "protege /api/admin/**" .-> ATD
```

| Contexto | Tipo | Responsabilidade | Relação |
|---|---|---|---|
| Atendimento | Core | Ciclo de vida da OS, orçamento, aprovação, status, tempo médio | Downstream (consumidor) de Cadastro, Catálogo e Estoque — *Customer/Supplier* |
| Cadastro | Suporte | Clientes (CPF/CNPJ) e veículos (placa) | Upstream |
| Catálogo de Serviços | Suporte | Serviços com preço e tempo estimado | Upstream; o preço é copiado para a OS (*Published Language* via id + snapshot) |
| Estoque | Suporte | Saldo de peças/insumos, entradas, saídas, alerta de mínimo | Upstream na inclusão; reage à aprovação do orçamento |
| Identidade e Acesso | Genérico | Login e emissão/validação de JWT | Conformista (Spring Security) |

## 2. Agregados, entidades e objetos de valor

```mermaid
classDiagram
    direction LR
    class OrdemServico {
        <<Aggregate Root>>
        UUID id
        UUID clienteId
        UUID veiculoId
        StatusOrdemServico status
        String descricaoProblema
        String diagnostico
        SituacaoOrcamento situacaoOrcamento
        Instant abertaEm / inicioExecucao / fimExecucao / entregueEm
        +abrir()$
        +iniciarDiagnostico()
        +adicionarServico() / removerServico()
        +adicionarPeca() / removerPeca()
        +getOrcamento() Orcamento
        +enviarOrcamento()
        +aprovarOrcamento()
        +rejeitarOrcamento()
        +finalizar()
        +entregar()
        +duracaoExecucao()
    }
    class ItemServico {
        <<Entity>>
        UUID id
        UUID servicoId
        String descricao
        Dinheiro precoUnitario
        int quantidade
        +subtotal()
    }
    class ItemPeca {
        <<Entity>>
        UUID id
        UUID pecaId
        String descricao
        Dinheiro precoUnitario
        int quantidade
        +subtotal()
    }
    class HistoricoStatus {
        <<Value Object>>
        StatusOrdemServico status
        Instant ocorridoEm
    }
    class Orcamento {
        <<Value Object>>
        Dinheiro valorServicos
        Dinheiro valorPecas
        Dinheiro valorTotal
        SituacaoOrcamento situacao
    }
    class StatusOrdemServico {
        <<Enumeration>>
        RECEBIDA
        EM_DIAGNOSTICO
        AGUARDANDO_APROVACAO
        EM_EXECUCAO
        FINALIZADA
        ENTREGUE
    }

    class Cliente {
        <<Aggregate Root>>
        UUID id
        String nome
        Documento documento
        Email email
        String telefone
    }
    class Documento {
        <<Value Object>>
        String numero
        TipoDocumento tipo
        +de(String)$
        +formatado()
    }
    class Email {
        <<Value Object>>
        String endereco
    }
    class Veiculo {
        <<Aggregate Root>>
        UUID id
        Placa placa
        String marca
        String modelo
        int ano
        UUID clienteId
    }
    class Placa {
        <<Value Object>>
        String valor
        +mercosul()
    }
    class Servico {
        <<Aggregate Root>>
        UUID id
        String nome
        Dinheiro preco
        int tempoEstimadoMinutos
    }
    class Peca {
        <<Aggregate Root>>
        UUID id
        String codigo
        TipoPeca tipo
        Dinheiro preco
        int quantidadeEstoque
        int estoqueMinimo
        +baixar()
        +repor()
    }
    class Dinheiro {
        <<Value Object>>
        BigDecimal valor
        +somar()
        +multiplicar()
    }

    OrdemServico *-- "0..*" ItemServico
    OrdemServico *-- "0..*" ItemPeca
    OrdemServico *-- "1..*" HistoricoStatus
    OrdemServico ..> Orcamento : calcula
    OrdemServico --> StatusOrdemServico
    Cliente *-- Documento
    Cliente *-- Email
    Veiculo *-- Placa
    OrdemServico ..> Cliente : clienteId
    OrdemServico ..> Veiculo : veiculoId
    ItemServico ..> Servico : servicoId
    ItemPeca ..> Peca : pecaId
    Veiculo ..> Cliente : clienteId
    Peca *-- Dinheiro
    Servico *-- Dinheiro
```

**Serviço de domínio:** `CalculadoraTempoMedioExecucao` (tempo médio geral e por serviço, a partir das OS executadas).
**Portas de repositório (domínio):** `ClienteRepository`, `VeiculoRepository`, `ServicoRepository`, `PecaRepository`, `OrdemServicoRepository`.

## 3. Ciclo de vida da Ordem de Serviço (máquina de estados)

```mermaid
stateDiagram-v2
    [*] --> RECEBIDA : abrir OS
    RECEBIDA --> EM_DIAGNOSTICO : iniciar diagnóstico
    EM_DIAGNOSTICO --> AGUARDANDO_APROVACAO : enviar orçamento<br/>(≥1 item e estoque disponível)
    AGUARDANDO_APROVACAO --> EM_EXECUCAO : aprovar orçamento<br/>(baixa estoque)
    AGUARDANDO_APROVACAO --> FINALIZADA : rejeitar orçamento<br/>(sem execução)
    EM_EXECUCAO --> FINALIZADA : finalizar execução
    FINALIZADA --> ENTREGUE : entregar veículo
    ENTREGUE --> [*]
    note right of RECEBIDA : itens (serviços/peças)\nsó podem mudar em\nRECEBIDA e EM_DIAGNOSTICO
```

## 4. Eventos de domínio

| Evento | Emitido por | Quando | Reação (política) |
|---|---|---|---|
| `OrdemServicoAberta` | `OrdemServico.abrir()` | OS criada | — |
| `StatusOrdemServicoAlterado` | toda transição de status | mudança de etapa | notificar o cliente (`NotificacoesListener`) |
| `OrcamentoEnviado` | `enviarOrcamento()` | orçamento disponível | notificar o cliente para aprovação |
| `OrcamentoAprovado` | `aprovarOrcamento()` | cliente autorizou | iniciar execução + baixar estoque (mesma transação) |
| `OrcamentoRejeitado` | `rejeitarOrcamento()` | cliente recusou | finalizar sem execução |
| `OrdemServicoFinalizada` | `finalizar()` | execução concluída | avisar que o veículo está pronto |
| `VeiculoEntregue` | `entregar()` | veículo devolvido | encerra a OS |
| `EstoqueAbaixoDoMinimo` | `Peca.baixar()` | saldo < mínimo | alerta de reposição |

Os eventos são acumulados no agregado (`AgregadoRaiz`), publicados pela aplicação através da porta
`PublicadorEventos` e tratados **após o commit** (`@TransactionalEventListener`).

## 5. Arquitetura: Clean Architecture em monólito

```mermaid
flowchart TB
    subgraph EXT["Adaptadores (frameworks)"]
        direction LR
        REST["<b>interfaces.rest</b><br/>Controllers, DTOs, ProblemDetail,<br/>OpenAPI"]
        INFRA["<b>infrastructure</b><br/>JPA (entities, adapters), Flyway,<br/>Spring Security/JWT, eventos,<br/>wiring + transações"]
    end
    subgraph APP["Aplicação"]
        UC["<b>application</b><br/>Casos de uso (AbrirOrdemServico, ResponderOrcamento, ...)<br/>Portas de saída: PublicadorEventos, AutenticacaoPort"]
    end
    subgraph DOM["Domínio (Java puro)"]
        D["<b>domain</b><br/>Agregados, Entidades, Objetos de Valor,<br/>Eventos, Serviços de domínio, Portas de repositório"]
    end
    REST --> UC
    INFRA --> UC
    INFRA -. implementa portas .-> D
    UC --> D
```

A regra de dependência (setas sempre para dentro) é verificada automaticamente pelo teste
`ArquiteturaTest` (ArchUnit): o domínio e a aplicação **não importam** Spring, JPA nem Jackson.

## 6. Sequência: aprovação do orçamento pelo cliente

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant API as AcompanhamentoClienteController
    participant TX as TransactionInterceptor
    participant UC as ResponderOrcamentoUseCase
    participant OS as OrdemServico (agregado)
    participant PR as PecaRepository
    participant P as Peca (agregado)
    participant EV as PublicadorEventos
    C->>API: POST /api/publico/ordens-servico/{id}/orcamento/resposta<br/>X-Documento-Cliente, {aprovado:true}
    API->>TX: aprovarPeloCliente(id, documento)
    TX->>UC: BEGIN
    UC->>UC: obterDoCliente(id, documento)
    UC->>OS: aprovarOrcamento(agora)
    OS-->>UC: status EM_EXECUCAO + eventos
    loop cada peça (ordenadas por id)
        UC->>PR: buscarPorIdParaAtualizacao (SELECT ... FOR UPDATE)
        UC->>P: baixar(qtd)
        alt saldo insuficiente
            P-->>UC: EstoqueInsuficienteException
            UC-->>TX: exceção ⇒ ROLLBACK (nada é gravado)
            TX-->>API: 422 Regra de negócio violada
        end
        UC->>PR: salvar(peça)
    end
    UC->>EV: publicar(eventos)
    TX->>TX: COMMIT
    EV-->>C: notificação (após commit)
    API-->>C: 200 OS em EM_EXECUCAO
```

## 7. Modelo de dados (PostgreSQL)

```mermaid
erDiagram
    clientes ||--o{ veiculos : possui
    clientes ||--o{ ordens_servico : solicita
    veiculos ||--o{ ordens_servico : atendido_em
    ordens_servico ||--o{ os_itens_servico : contem
    ordens_servico ||--o{ os_itens_peca : contem
    ordens_servico ||--|{ os_historico_status : registra
    servicos ||--o{ os_itens_servico : referenciado
    pecas ||--o{ os_itens_peca : referenciado
    clientes {
        uuid id PK
        varchar documento UK
        varchar tipo_documento
        varchar nome
        varchar email
    }
    veiculos {
        uuid id PK
        varchar placa UK
        varchar marca
        varchar modelo
        int ano
        uuid cliente_id FK
    }
    servicos {
        uuid id PK
        varchar nome
        numeric preco
        int tempo_estimado_minutos
    }
    pecas {
        uuid id PK
        varchar codigo UK
        varchar tipo
        numeric preco
        int quantidade_estoque
        int estoque_minimo
    }
    ordens_servico {
        uuid id PK
        uuid cliente_id FK
        uuid veiculo_id FK
        varchar status
        varchar situacao_orcamento
        timestamptz inicio_execucao
        timestamptz fim_execucao
        bigint versao
    }
    os_itens_servico {
        uuid id PK
        uuid ordem_servico_id FK
        uuid servico_id FK
        numeric preco_unitario
        int quantidade
    }
    os_itens_peca {
        uuid id PK
        uuid ordem_servico_id FK
        uuid peca_id FK
        numeric preco_unitario
        int quantidade
    }
    os_historico_status {
        uuid id PK
        uuid ordem_servico_id FK
        varchar status
        timestamptz ocorrido_em
    }
    usuarios {
        uuid id PK
        varchar username UK
        varchar senha_hash
        varchar perfil
    }
```

## 8. Relação documentação ↔ código

| Elemento DDD | Implementação |
|---|---|
| Agregado Ordem de Serviço | `src/main/java/br/com/oficina/domain/ordemservico/OrdemServico.java` |
| Regras de transição | `StatusOrdemServico.proximosPermitidos()` + `OrdemServico.alterarStatus()` |
| Objetos de valor | `Documento`, `Placa`, `Dinheiro`, `Email`, `Orcamento`, `HistoricoStatus` |
| Eventos de domínio | `domain/ordemservico/evento/*`, `domain/estoque/EstoqueAbaixoDoMinimo` |
| Políticas | `ResponderOrcamentoUseCase` (baixa de estoque), `NotificacoesListener` (notificações) |
| Comandos | métodos públicos dos casos de uso em `application/**` ↔ endpoints em `interfaces/rest/**` |
| Repositórios (portas/adaptadores) | `domain/**/…Repository` ↔ `infrastructure/persistence/adapter/*` |
| Serviço de domínio | `CalculadoraTempoMedioExecucao` |
| Testes das regras | `src/test/java/br/com/oficina/domain/**` (100% de linhas cobertas) |
