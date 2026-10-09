package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "os_historico_status")
public class HistoricoStatusEntity {

    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServicoEntity ordemServico;
    @Column(nullable = false)
    private String status;
    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected HistoricoStatusEntity() {
    }

    public HistoricoStatusEntity(UUID id, OrdemServicoEntity ordemServico, String status, Instant ocorridoEm) {
        this.id = id;
        this.ordemServico = ordemServico;
        this.status = status;
        this.ocorridoEm = ocorridoEm;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public Instant getOcorridoEm() { return ocorridoEm; }
}
