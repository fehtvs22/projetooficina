package br.com.oficina.infrastructure.persistence.adapter;

import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.servico.ServicoRepository;
import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.infrastructure.persistence.entity.ServicoEntity;
import br.com.oficina.infrastructure.persistence.jpa.ServicoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServicoRepositoryAdapter implements ServicoRepository {

    private final ServicoJpaRepository jpa;

    public ServicoRepositoryAdapter(ServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Servico salvar(Servico s) {
        jpa.save(new ServicoEntity(s.getId(), s.getNome(), s.getDescricao(), s.getPreco().valor(),
                s.getTempoEstimadoMinutos()));
        return s;
    }

    @Override
    public Optional<Servico> buscarPorId(UUID id) {
        return jpa.findById(id).map(ServicoRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Servico> listar() {
        return jpa.findAllByOrderByNomeAsc().stream().map(ServicoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Servico paraDominio(ServicoEntity e) {
        return Servico.reconstituir(e.getId(), e.getNome(), e.getDescricao(), Dinheiro.de(e.getPreco()),
                e.getTempoEstimadoMinutos());
    }
}
