package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ordens_servico")
public class OrdemServicoEntity {

    @Id
    private UUID id;
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;
    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;
    @Column(nullable = false)
    private String status;
    @Column(name = "descricao_problema")
    private String descricaoProblema;
    private String diagnostico;
    @Column(name = "situacao_orcamento", nullable = false)
    private String situacaoOrcamento;
    @Column(name = "orcamento_enviado_em")
    private Instant orcamentoEnviadoEm;
    @Column(name = "orcamento_respondido_em")
    private Instant orcamentoRespondidoEm;
    @Column(name = "motivo_rejeicao")
    private String motivoRejeicao;
    @Column(name = "aberta_em", nullable = false)
    private Instant abertaEm;
    @Column(name = "inicio_execucao")
    private Instant inicioExecucao;
    @Column(name = "fim_execucao")
    private Instant fimExecucao;
    @Column(name = "entregue_em")
    private Instant entregueEm;
    @Version
    private Long versao;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemServicoEntity> servicos = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPecaEntity> pecas = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ocorridoEm ASC")
    private List<HistoricoStatusEntity> historico = new ArrayList<>();

    protected OrdemServicoEntity() {
    }

    public OrdemServicoEntity(UUID id) {
        this.id = id;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
    public UUID getVeiculoId() { return veiculoId; }
    public void setVeiculoId(UUID veiculoId) { this.veiculoId = veiculoId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescricaoProblema() { return descricaoProblema; }
    public void setDescricaoProblema(String descricaoProblema) { this.descricaoProblema = descricaoProblema; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getSituacaoOrcamento() { return situacaoOrcamento; }
    public void setSituacaoOrcamento(String situacaoOrcamento) { this.situacaoOrcamento = situacaoOrcamento; }
    public Instant getOrcamentoEnviadoEm() { return orcamentoEnviadoEm; }
    public void setOrcamentoEnviadoEm(Instant v) { this.orcamentoEnviadoEm = v; }
    public Instant getOrcamentoRespondidoEm() { return orcamentoRespondidoEm; }
    public void setOrcamentoRespondidoEm(Instant v) { this.orcamentoRespondidoEm = v; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }
    public Instant getAbertaEm() { return abertaEm; }
    public void setAbertaEm(Instant abertaEm) { this.abertaEm = abertaEm; }
    public Instant getInicioExecucao() { return inicioExecucao; }
    public void setInicioExecucao(Instant v) { this.inicioExecucao = v; }
    public Instant getFimExecucao() { return fimExecucao; }
    public void setFimExecucao(Instant v) { this.fimExecucao = v; }
    public Instant getEntregueEm() { return entregueEm; }
    public void setEntregueEm(Instant v) { this.entregueEm = v; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public List<ItemServicoEntity> getServicos() { return servicos; }
    public List<ItemPecaEntity> getPecas() { return pecas; }
    public List<HistoricoStatusEntity> getHistorico() { return historico; }
}
