package br.com.oficina.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Usuário administrativo (atendente/gestor). É um conceito de segurança, não do domínio da oficina.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;
    @Column(nullable = false)
    private String perfil;
    private boolean ativo;

    protected UsuarioEntity() {
    }

    public UsuarioEntity(UUID id, String username, String senhaHash, String perfil, boolean ativo) {
        this.id = id;
        this.username = username;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = ativo;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getSenhaHash() { return senhaHash; }
    public String getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
}
