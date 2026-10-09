package br.com.oficina.application.ordemservico;

import br.com.oficina.domain.ordemservico.CalculadoraTempoMedioExecucao;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.ordemservico.TempoMedioExecucao;

/**
 * Caso de uso: monitoramento do tempo médio de execução dos serviços.
 */
public class CalcularTempoMedioExecucaoUseCase {

    private final OrdemServicoRepository ordens;
    private final CalculadoraTempoMedioExecucao calculadora = new CalculadoraTempoMedioExecucao();

    public CalcularTempoMedioExecucaoUseCase(OrdemServicoRepository ordens) {
        this.ordens = ordens;
    }

    public TempoMedioExecucao calcular() {
        return calculadora.calcular(ordens.listarComExecucaoConcluida());
    }
}
