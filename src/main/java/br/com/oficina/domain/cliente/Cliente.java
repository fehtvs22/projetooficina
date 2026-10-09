package br.com.oficina.domain.cliente;

import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.Email;

import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Cliente: pessoa física ou jurídica dona de veículos atendidos pela oficina.
 */
public class Cliente {

    private final UUID id;
    private String nome;
    private final Documento documento;
    private Email email;
    private String telefone;

    private Cliente(UUID id, String nome, Documento documento, Email email, String telefone) {
        this.id = Assercoes.obrigatorio(id, "Id do cliente");
        this.nome = Assercoes.textoObrigatorio(nome, "Nome do cliente");
        this.documento = Assercoes.obrigatorio(documento, "Documento do cliente");
        this.email = email;
        this.telefone = Assercoes.textoOpcional(telefone);
    }

    public static Cliente cadastrar(String nome, Documento documento, Email email, String telefone) {
        return new Cliente(UUID.randomUUID(), nome, documento, email, telefone);
    }

    public static Cliente reconstituir(UUID id, String nome, Documento documento, Email email, String telefone) {
        return new Cliente(id, nome, documento, email, telefone);
    }

    public void atualizarContato(String nome, Email email, String telefone) {
        this.nome = Assercoes.textoObrigatorio(nome, "Nome do cliente");
        this.email = email;
        this.telefone = Assercoes.textoOpcional(telefone);
    }

    public boolean possuiDocumento(Documento outro) {
        return documento.equals(outro);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Documento getDocumento() {
        return documento;
    }

    public Email getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cliente outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
