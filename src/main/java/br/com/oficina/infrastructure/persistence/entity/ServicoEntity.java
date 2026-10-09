package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servicos")
public class ServicoEntity {

    @Id
    private UUID id;
    @Column(nullable = false)
    private String nome;
    private String descricao;
    @Column(nullable = false)
    private BigDecimal preco;
    @Column(name = "tempo_estimado_minutos")
    private int tempoEstimadoMinutos;

    protected ServicoEntity() {
    }

    public ServicoEntity(UUID id, String nome, String descricao, BigDecimal preco, int tempoEstimadoMinutos) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public int getTempoEstimadoMinutos() { return tempoEstimadoMinutos; }
}
