package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.shared.Dinheiro;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraTempoMedioExecucaoTest {

    private static final Instant T0 = Instant.parse("2026-03-01T08:00:00Z");
    private static final UUID TROCA_OLEO = UUID.randomUUID();
    private static final UUID FREIOS = UUID.randomUUID();

    private final CalculadoraTempoMedioExecucao calculadora = new CalculadoraTempoMedioExecucao();

    private OrdemServico executada(int minutos, UUID... servicos) {
        OrdemServico os = OrdemServico.abrir(UUID.randomUUID(), UUID.randomUUID(), null, T0);
        for (UUID s : servicos) {
            os.adicionarServico(s, s == TROCA_OLEO ? "Troca de óleo" : "Revisão de freios", Dinheiro.de("100"), 1);
        }
        os.iniciarDiagnostico(T0);
        os.enviarOrcamento(T0);
        os.aprovarOrcamento(T0);
        os.finalizar(T0.plusSeconds(minutos * 60L));
        return os;
    }

    @Test
    void semOrdensExecutadas() {
        OrdemServico naoExecutada = OrdemServico.abrir(UUID.randomUUID(), UUID.randomUUID(), null, T0);

        TempoMedioExecucao resultado = calculadora.calcular(List.of(naoExecutada));

        assertThat(resultado.ordensConsideradas()).isZero();
        assertThat(resultado.mediaGeralMinutos()).isZero();
        assertThat(resultado.porServico()).isEmpty();
    }

    @Test
    void calculaMediaGeralEPorServico() {
        TempoMedioExecucao resultado = calculadora.calcular(List.of(
                executada(30, TROCA_OLEO),
                executada(60, TROCA_OLEO, FREIOS),
                executada(100, FREIOS)));

        assertThat(resultado.ordensConsideradas()).isEqualTo(3);
        assertThat(resultado.mediaGeralMinutos()).isEqualTo(63.33);
        assertThat(resultado.porServico()).extracting(TempoMedioExecucao.PorServico::servico)
                .containsExactly("Revisão de freios", "Troca de óleo");
        assertThat(resultado.porServico().get(0).mediaMinutos()).isEqualTo(80.0);
        assertThat(resultado.porServico().get(0).ordensConsideradas()).isEqualTo(2);
        assertThat(resultado.porServico().get(1).mediaMinutos()).isEqualTo(45.0);
        assertThat(resultado.porServico().get(1).servicoId()).isEqualTo(TROCA_OLEO);
    }
}
