package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.domain.ordemservico.TempoMedioExecucao;

import java.util.List;
import java.util.UUID;

public record TempoMedioExecucaoResponse(long ordensConsideradas, double mediaGeralMinutos,
                                         List<PorServico> porServico) {

    public record PorServico(UUID servicoId, String servico, long ordensConsideradas, double mediaMinutos) {
    }

    public static TempoMedioExecucaoResponse de(TempoMedioExecucao t) {
        return new TempoMedioExecucaoResponse(t.ordensConsideradas(), t.mediaGeralMinutos(),
                t.porServico().stream()
                        .map(p -> new PorServico(p.servicoId(), p.servico(), p.ordensConsideradas(), p.mediaMinutos()))
                        .toList());
    }
}
