package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pecas")
public class PecaEntity {

    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String codigo;
    @Column(nullable = false)
    private String nome;
    private String descricao;
    @Column(nullable = false)
    private String tipo;
    @Column(nullable = false)
    private BigDecimal preco;
    @Column(name = "quantidade_estoque")
    private int quantidadeEstoque;
    @Column(name = "estoque_minimo")
    private int estoqueMinimo;

    protected PecaEntity() {
    }

    public PecaEntity(UUID id, String codigo, String nome, String descricao, String tipo, BigDecimal preco,
                      int quantidadeEstoque, int estoqueMinimo) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getTipo() { return tipo; }
    public BigDecimal getPreco() { return preco; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public int getEstoqueMinimo() { return estoqueMinimo; }
}
