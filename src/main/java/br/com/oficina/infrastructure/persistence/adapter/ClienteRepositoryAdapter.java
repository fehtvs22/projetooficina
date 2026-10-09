package br.com.oficina.infrastructure.persistence.adapter;

import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.cliente.TipoDocumento;
import br.com.oficina.domain.shared.Email;
import br.com.oficina.infrastructure.persistence.entity.ClienteEntity;
import br.com.oficina.infrastructure.persistence.jpa.ClienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpa;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        jpa.save(new ClienteEntity(cliente.getId(), cliente.getNome(), cliente.getDocumento().numero(),
                cliente.getDocumento().tipo().name(),
                cliente.getEmail() == null ? null : cliente.getEmail().endereco(), cliente.getTelefone()));
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return jpa.findById(id).map(ClienteRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(Documento documento) {
        return jpa.findByDocumento(documento.numero()).map(ClienteRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Cliente> listar() {
        return jpa.findAllByOrderByNomeAsc().stream().map(ClienteRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    static Cliente paraDominio(ClienteEntity e) {
        return Cliente.reconstituir(e.getId(), e.getNome(),
                new Documento(e.getDocumento(), TipoDocumento.valueOf(e.getTipoDocumento())),
                Email.deOpcional(e.getEmail()), e.getTelefone());
    }
}
