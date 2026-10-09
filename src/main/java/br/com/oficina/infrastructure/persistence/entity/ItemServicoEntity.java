package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "os_itens_servico")
public class ItemServicoEntity {

    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServicoEntity ordemServico;
    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;
    @Column(nullable = false)
    private String descricao;
    @Column(name = "preco_unitario", nullable = false)
    private BigDecimal precoUnitario;
    private int quantidade;

    protected ItemServicoEntity() {
    }

    public ItemServicoEntity(UUID id, OrdemServicoEntity ordemServico, UUID servicoId, String descricao,
                             BigDecimal precoUnitario, int quantidade) {
        this.id = id;
        this.ordemServico = ordemServico;
        this.servicoId = servicoId;
        this.descricao = descricao;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public UUID getId() { return id; }
    public UUID getServicoId() { return servicoId; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
