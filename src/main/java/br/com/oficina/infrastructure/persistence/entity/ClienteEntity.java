package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    private UUID id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, unique = true)
    private String documento;
    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;
    private String email;
    private String telefone;

    protected ClienteEntity() {
    }

    public ClienteEntity(UUID id, String nome, String documento, String tipoDocumento, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.email = email;
        this.telefone = telefone;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
}
