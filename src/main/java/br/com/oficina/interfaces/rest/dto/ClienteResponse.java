package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.domain.cliente.Cliente;

import java.util.UUID;

public record ClienteResponse(UUID id, String nome, String documento, String tipoDocumento, String email,
                              String telefone) {

    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getDocumento().formatado(), c.getDocumento().tipo().name(),
                c.getEmail() == null ? null : c.getEmail().endereco(), c.getTelefone());
    }
}
