package br.com.oficina.domain.ordemservico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(UUID id);

    /**
     * Lista as ordens de serviço, opcionalmente filtrando por status, das mais antigas para as mais novas.
     */
    List<OrdemServico> listar(StatusOrdemServico status);

    List<OrdemServico> listarPorCliente(UUID clienteId);

    /**
     * Ordens que tiveram a execução concluída (possuem início e fim de execução).
     */
    List<OrdemServico> listarComExecucaoConcluida();

    boolean existeParaVeiculo(UUID veiculoId);
}
