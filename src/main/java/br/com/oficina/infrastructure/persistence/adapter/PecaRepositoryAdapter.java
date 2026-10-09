package br.com.oficina.infrastructure.persistence.adapter;

import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.estoque.TipoPeca;
import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.infrastructure.persistence.entity.PecaEntity;
import br.com.oficina.infrastructure.persistence.jpa.PecaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PecaRepositoryAdapter implements PecaRepository {

    private final PecaJpaRepository jpa;

    public PecaRepositoryAdapter(PecaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Peca salvar(Peca p) {
        jpa.save(new PecaEntity(p.getId(), p.getCodigo(), p.getNome(), p.getDescricao(), p.getTipo().name(),
                p.getPreco().valor(), p.getQuantidadeEstoque(), p.getEstoqueMinimo()));
        return p;
    }

    @Override
    public Optional<Peca> buscarPorId(UUID id) {
        return jpa.findById(id).map(PecaRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<Peca> buscarPorIdParaAtualizacao(UUID id) {
        return jpa.findByIdForUpdate(id).map(PecaRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<Peca> buscarPorCodigo(String codigo) {
        return jpa.findByCodigo(codigo).map(PecaRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Peca> listar() {
        return jpa.findAllByOrderByNomeAsc().stream().map(PecaRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Peca paraDominio(PecaEntity e) {
        return Peca.reconstituir(e.getId(), e.getCodigo(), e.getNome(), e.getDescricao(), TipoPeca.valueOf(e.getTipo()),
                Dinheiro.de(e.getPreco()), e.getQuantidadeEstoque(), e.getEstoqueMinimo());
    }
}
