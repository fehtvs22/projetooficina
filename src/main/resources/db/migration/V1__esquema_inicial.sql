-- Esquema inicial do Sistema Integrado de Atendimento e Execução de Serviços

CREATE TABLE clientes (
    id             UUID PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    documento      VARCHAR(14)  NOT NULL,
    tipo_documento VARCHAR(4)   NOT NULL,
    email          VARCHAR(150),
    telefone       VARCHAR(20),
    CONSTRAINT uk_clientes_documento UNIQUE (documento)
);

CREATE TABLE veiculos (
    id         UUID PRIMARY KEY,
    placa      VARCHAR(7)   NOT NULL,
    marca      VARCHAR(60)  NOT NULL,
    modelo     VARCHAR(80)  NOT NULL,
    ano        INTEGER      NOT NULL,
    cliente_id UUID         NOT NULL,
    CONSTRAINT uk_veiculos_placa UNIQUE (placa),
    CONSTRAINT fk_veiculos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);
CREATE INDEX ix_veiculos_cliente ON veiculos (cliente_id);

CREATE TABLE servicos (
    id                     UUID PRIMARY KEY,
    nome                   VARCHAR(120)   NOT NULL,
    descricao              VARCHAR(500),
    preco                  NUMERIC(12, 2) NOT NULL,
    tempo_estimado_minutos INTEGER        NOT NULL
);

CREATE TABLE pecas (
    id                 UUID PRIMARY KEY,
    codigo             VARCHAR(40)    NOT NULL,
    nome               VARCHAR(120)   NOT NULL,
    descricao          VARCHAR(500),
    tipo               VARCHAR(10)    NOT NULL,
    preco              NUMERIC(12, 2) NOT NULL,
    quantidade_estoque INTEGER        NOT NULL,
    estoque_minimo     INTEGER        NOT NULL,
    CONSTRAINT uk_pecas_codigo UNIQUE (codigo),
    CONSTRAINT ck_pecas_estoque CHECK (quantidade_estoque >= 0)
);

CREATE TABLE ordens_servico (
    id                      UUID PRIMARY KEY,
    cliente_id              UUID        NOT NULL,
    veiculo_id              UUID        NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    descricao_problema      VARCHAR(1000),
    diagnostico             VARCHAR(2000),
    situacao_orcamento      VARCHAR(30) NOT NULL,
    orcamento_enviado_em    TIMESTAMP WITH TIME ZONE,
    orcamento_respondido_em TIMESTAMP WITH TIME ZONE,
    motivo_rejeicao         VARCHAR(500),
    aberta_em               TIMESTAMP WITH TIME ZONE NOT NULL,
    inicio_execucao         TIMESTAMP WITH TIME ZONE,
    fim_execucao            TIMESTAMP WITH TIME ZONE,
    entregue_em             TIMESTAMP WITH TIME ZONE,
    versao                  BIGINT      NOT NULL,
    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
);
CREATE INDEX ix_os_status ON ordens_servico (status);
CREATE INDEX ix_os_cliente ON ordens_servico (cliente_id);
CREATE INDEX ix_os_veiculo ON ordens_servico (veiculo_id);

CREATE TABLE os_itens_servico (
    id                UUID PRIMARY KEY,
    ordem_servico_id  UUID           NOT NULL,
    servico_id        UUID           NOT NULL,
    descricao         VARCHAR(120)   NOT NULL,
    preco_unitario    NUMERIC(12, 2) NOT NULL,
    quantidade        INTEGER        NOT NULL,
    CONSTRAINT fk_itserv_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
    CONSTRAINT fk_itserv_servico FOREIGN KEY (servico_id) REFERENCES servicos (id)
);
CREATE INDEX ix_itserv_os ON os_itens_servico (ordem_servico_id);

CREATE TABLE os_itens_peca (
    id                UUID PRIMARY KEY,
    ordem_servico_id  UUID           NOT NULL,
    peca_id           UUID           NOT NULL,
    descricao         VARCHAR(120)   NOT NULL,
    preco_unitario    NUMERIC(12, 2) NOT NULL,
    quantidade        INTEGER        NOT NULL,
    CONSTRAINT fk_itpeca_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
    CONSTRAINT fk_itpeca_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);
CREATE INDEX ix_itpeca_os ON os_itens_peca (ordem_servico_id);

CREATE TABLE os_historico_status (
    id               UUID PRIMARY KEY,
    ordem_servico_id UUID        NOT NULL,
    status           VARCHAR(30) NOT NULL,
    ocorrido_em      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_hist_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE
);
CREATE INDEX ix_hist_os ON os_historico_status (ordem_servico_id);

CREATE TABLE usuarios (
    id         UUID PRIMARY KEY,
    username   VARCHAR(60)  NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL,
    ativo      BOOLEAN      NOT NULL,
    CONSTRAINT uk_usuarios_username UNIQUE (username)
);
