package br.com.oficina.infrastructure.persistence.adapter;

import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.domain.veiculo.VeiculoRepository;
import br.com.oficina.infrastructure.persistence.entity.VeiculoEntity;
import br.com.oficina.infrastructure.persistence.jpa.VeiculoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VeiculoRepositoryAdapter implements VeiculoRepository {

    private final VeiculoJpaRepository jpa;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Veiculo salvar(Veiculo v) {
        jpa.save(new VeiculoEntity(v.getId(), v.getPlaca().valor(), v.getMarca(), v.getModelo(), v.getAno(),
                v.getClienteId()));
        return v;
    }

    @Override
    public Optional<Veiculo> buscarPorId(UUID id) {
        return jpa.findById(id).map(VeiculoRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        return jpa.findByPlaca(placa.valor()).map(VeiculoRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Veiculo> listar() {
        return jpa.findAllByOrderByPlacaAsc().stream().map(VeiculoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public List<Veiculo> listarPorCliente(UUID clienteId) {
        return jpa.findByClienteIdOrderByPlacaAsc(clienteId).stream().map(VeiculoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Veiculo paraDominio(VeiculoEntity e) {
        return Veiculo.reconstituir(e.getId(), new Placa(e.getPlaca()), e.getMarca(), e.getModelo(), e.getAno(),
                e.getClienteId());
    }
}
