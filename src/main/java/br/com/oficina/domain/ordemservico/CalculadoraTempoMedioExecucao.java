package br.com.oficina.domain.ordemservico;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço de domínio: calcula o tempo médio de execução a partir das ordens já executadas.
 * Para a média por serviço considera-se a duração total da OS em que o serviço foi realizado.
 */
public class CalculadoraTempoMedioExecucao {

    public TempoMedioExecucao calcular(List<OrdemServico> ordens) {
        List<OrdemServico> executadas = ordens.stream().filter(os -> os.duracaoExecucao().isPresent()).toList();
        if (executadas.isEmpty()) {
            return new TempoMedioExecucao(0, 0, List.of());
        }

        double mediaGeral = executadas.stream().mapToLong(CalculadoraTempoMedioExecucao::minutos)
                .average().orElse(0);

        Map<UUID, Acumulador> porServico = new LinkedHashMap<>();
        for (OrdemServico os : executadas) {
            long minutos = minutos(os);
            os.getServicos().forEach(item -> porServico
                    .computeIfAbsent(item.getServicoId(), id -> new Acumulador(item.getDescricao()))
                    .adicionar(minutos));
        }

        List<TempoMedioExecucao.PorServico> resultado = new ArrayList<>();
        porServico.forEach((id, acc) -> resultado.add(
                new TempoMedioExecucao.PorServico(id, acc.descricao, acc.quantidade, arredondar(acc.media()))));
        resultado.sort(Comparator.comparing(TempoMedioExecucao.PorServico::servico));

        return new TempoMedioExecucao(executadas.size(), arredondar(mediaGeral), resultado);
    }

    private static long minutos(OrdemServico os) {
        return os.duracaoExecucao().map(Duration::toMinutes).orElse(0L);
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private static final class Acumulador {
        private final String descricao;
        private long quantidade;
        private long totalMinutos;

        private Acumulador(String descricao) {
            this.descricao = descricao;
        }

        private void adicionar(long minutos) {
            quantidade++;
            totalMinutos += minutos;
        }

        private double media() {
            return (double) totalMinutos / quantidade;
        }
    }
}
