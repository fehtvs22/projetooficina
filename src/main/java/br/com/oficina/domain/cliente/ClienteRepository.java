package br.com.oficina.domain.cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência do agregado Cliente (implementada na camada de infraestrutura).
 */
public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorDocumento(Documento documento);

    List<Cliente> listar();

    void remover(UUID id);
}
