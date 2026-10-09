package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "veiculos")
public class VeiculoEntity {

    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String placa;
    @Column(nullable = false)
    private String marca;
    @Column(nullable = false)
    private String modelo;
    private int ano;
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    protected VeiculoEntity() {
    }

    public VeiculoEntity(UUID id, String placa, String marca, String modelo, int ano, UUID clienteId) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.clienteId = clienteId;
    }

    public UUID getId() { return id; }
    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getAno() { return ano; }
    public UUID getClienteId() { return clienteId; }
}
