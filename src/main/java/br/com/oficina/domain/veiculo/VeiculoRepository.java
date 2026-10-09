package br.com.oficina.domain.veiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(UUID id);

    Optional<Veiculo> buscarPorPlaca(Placa placa);

    List<Veiculo> listar();

    List<Veiculo> listarPorCliente(UUID clienteId);

    void remover(UUID id);
}
