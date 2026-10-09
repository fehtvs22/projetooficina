package br.com.oficina.domain.ordemservico;

import java.util.List;
import java.util.UUID;

/**
 * Objeto de valor com o tempo médio de execução (em minutos), geral e por serviço.
 */
public record TempoMedioExecucao(long ordensConsideradas, double mediaGeralMinutos, List<PorServico> porServico) {

    public record PorServico(UUID servicoId, String servico, long ordensConsideradas, double mediaMinutos) {
    }
}
