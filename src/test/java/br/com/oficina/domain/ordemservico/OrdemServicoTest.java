package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.ordemservico.evento.OrcamentoAprovado;
import br.com.oficina.domain.ordemservico.evento.OrcamentoEnviado;
import br.com.oficina.domain.ordemservico.evento.OrcamentoRejeitado;
import br.com.oficina.domain.ordemservico.evento.OrdemServicoAberta;
import br.com.oficina.domain.ordemservico.evento.OrdemServicoFinalizada;
import br.com.oficina.domain.ordemservico.evento.StatusOrdemServicoAlterado;
import br.com.oficina.domain.ordemservico.evento.VeiculoEntregue;
import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.EventoDominio;
import br.com.oficina.domain.shared.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdemServicoTest {

    private static final Instant T0 = Instant.parse("2026-03-01T08:00:00Z");
    private static final UUID TROCA_OLEO = UUID.randomUUID();
    private static final UUID ALINHAMENTO = UUID.randomUUID();
    private static final UUID OLEO = UUID.randomUUID();
    private static final UUID FILTRO = UUID.randomUUID();

    private final UUID clienteId = UUID.randomUUID();
    private final UUID veiculoId = UUID.randomUUID();
    private OrdemServico os;

    @BeforeEach
    void abrir() {
        os = OrdemServico.abrir(clienteId, veiculoId, "Barulho no motor", T0);
    }

    private OrdemServico comItensAguardandoAprovacao() {
        os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);
        os.adicionarPeca(OLEO, "Óleo 5W30", Dinheiro.de("45.90"), 4);
        os.iniciarDiagnostico(T0.plusSeconds(60));
        os.enviarOrcamento(T0.plusSeconds(120));
        os.extrairEventos();
        return os;
    }

    @Test
    void aberturaIniciaComoRecebidaComHistoricoEEvento() {
        assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(os.getAbertaEm()).isEqualTo(T0);
        assertThat(os.getDescricaoProblema()).isEqualTo("Barulho no motor");
        assertThat(os.getHistorico()).extracting(HistoricoStatus::status).containsExactly(StatusOrdemServico.RECEBIDA);
        assertThat(os.getOrcamento().situacao()).isEqualTo(SituacaoOrcamento.EM_ELABORACAO);
        assertThat(os.pertenceAoCliente(clienteId)).isTrue();
        assertThat(os.extrairEventos()).singleElement().isInstanceOf(OrdemServicoAberta.class);
    }

    @Nested
    class Itens {

        @Test
        void orcamentoCalculadoAutomaticamente() {
            os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);
            os.adicionarServico(ALINHAMENTO, "Alinhamento", Dinheiro.de("90"), 1);
            os.adicionarPeca(OLEO, "Óleo 5W30", Dinheiro.de("45.90"), 4);
            os.adicionarPeca(FILTRO, "Filtro", Dinheiro.de("38.50"), 1);

            Orcamento orcamento = os.getOrcamento();

            assertThat(orcamento.valorServicos().valor()).isEqualByComparingTo("210.00");
            assertThat(orcamento.valorPecas().valor()).isEqualByComparingTo("222.10");
            assertThat(orcamento.valorTotal().valor()).isEqualByComparingTo("432.10");
        }

        @Test
        void mesmoItemAcumulaQuantidade() {
            ItemServico s1 = os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);
            ItemServico s2 = os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 2);
            ItemPeca p1 = os.adicionarPeca(OLEO, "Óleo", Dinheiro.de("10"), 1);
            ItemPeca p2 = os.adicionarPeca(OLEO, "Óleo", Dinheiro.de("10"), 3);

            assertThat(s2).isSameAs(s1);
            assertThat(os.getServicos()).singleElement().extracting(ItemServico::getQuantidade).isEqualTo(3);
            assertThat(p2).isSameAs(p1);
            assertThat(os.quantidadeDaPeca(OLEO)).isEqualTo(4);
            assertThat(os.quantidadeDaPeca(FILTRO)).isZero();
        }

        @Test
        void removeItens() {
            ItemServico s = os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);
            ItemPeca p = os.adicionarPeca(OLEO, "Óleo", Dinheiro.de("10"), 1);

            os.removerServico(s.getId());
            os.removerPeca(p.getId());

            assertThat(os.getServicos()).isEmpty();
            assertThat(os.getPecas()).isEmpty();
            assertThat(os.getOrcamento().valorTotal()).isEqualTo(Dinheiro.ZERO);
        }

        @Test
        void removerItemInexistenteFalha() {
            assertThatThrownBy(() -> os.removerServico(UUID.randomUUID())).isInstanceOf(RegraNegocioException.class);
            assertThatThrownBy(() -> os.removerPeca(UUID.randomUUID())).isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void itensBloqueadosAposEnvioDoOrcamento() {
            comItensAguardandoAprovacao();

            assertThatThrownBy(() -> os.adicionarServico(ALINHAMENTO, "Alinhamento", Dinheiro.de("90"), 1))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Itens só podem ser alterados");
            assertThatThrownBy(() -> os.adicionarPeca(FILTRO, "Filtro", Dinheiro.de("1"), 1))
                    .isInstanceOf(RegraNegocioException.class);
            assertThatThrownBy(() -> os.removerServico(os.getServicos().getFirst().getId()))
                    .isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void listasExpostasSaoSomenteLeitura() {
            os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);
            assertThatThrownBy(() -> os.getServicos().clear()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> os.getHistorico().clear()).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class Fluxo {

        @Test
        void fluxoCompletoComAprovacao() {
            os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);
            os.iniciarDiagnostico(T0.plusSeconds(60));
            os.registrarDiagnostico("Óleo vencido");
            os.enviarOrcamento(T0.plusSeconds(120));
            os.aprovarOrcamento(T0.plusSeconds(600));
            os.finalizar(T0.plusSeconds(600 + 45 * 60));
            os.entregar(T0.plusSeconds(7200));

            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
            assertThat(os.getDiagnostico()).isEqualTo("Óleo vencido");
            assertThat(os.getOrcamento().situacao()).isEqualTo(SituacaoOrcamento.APROVADO);
            assertThat(os.getOrcamento().enviadoEm()).isEqualTo(T0.plusSeconds(120));
            assertThat(os.getOrcamento().respondidoEm()).isEqualTo(T0.plusSeconds(600));
            assertThat(os.getInicioExecucao()).isEqualTo(T0.plusSeconds(600));
            assertThat(os.getFimExecucao()).isEqualTo(T0.plusSeconds(600 + 45 * 60));
            assertThat(os.getEntregueEm()).isEqualTo(T0.plusSeconds(7200));
            assertThat(os.duracaoExecucao()).contains(Duration.ofMinutes(45));
            assertThat(os.getHistorico()).extracting(HistoricoStatus::status).containsExactly(
                    StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO,
                    StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO,
                    StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);

            List<EventoDominio> eventos = os.extrairEventos();
            assertThat(eventos).extracting(e -> (Class) e.getClass()).containsSubsequence(
                    OrdemServicoAberta.class, OrcamentoEnviado.class, OrcamentoAprovado.class,
                    OrdemServicoFinalizada.class, VeiculoEntregue.class);
            assertThat(eventos).filteredOn(StatusOrdemServicoAlterado.class::isInstance).hasSize(5);
        }

        @Test
        void envioDoOrcamentoGeraEventoComValor() {
            comItensAguardandoAprovacao();
            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
            assertThat(os.getOrcamento().situacao()).isEqualTo(SituacaoOrcamento.AGUARDANDO_APROVACAO);
        }

        @Test
        void orcamentoVazioNaoPodeSerEnviado() {
            os.iniciarDiagnostico(T0);

            assertThatThrownBy(() -> os.enviarOrcamento(T0))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("ao menos um serviço ou peça");
        }

        @Test
        void orcamentoSoPodeSerEnviadoEmDiagnostico() {
            os.adicionarServico(TROCA_OLEO, "Troca de óleo", Dinheiro.de("120"), 1);

            assertThatThrownBy(() -> os.enviarOrcamento(T0))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("enviar o orçamento");
        }

        @Test
        void rejeicaoFinalizaSemExecucao() {
            comItensAguardandoAprovacao();

            os.rejeitarOrcamento("Caro demais", T0.plusSeconds(300));

            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
            assertThat(os.getOrcamento().situacao()).isEqualTo(SituacaoOrcamento.REJEITADO);
            assertThat(os.getOrcamento().motivoRejeicao()).isEqualTo("Caro demais");
            assertThat(os.getInicioExecucao()).isNull();
            assertThat(os.duracaoExecucao()).isEmpty();
            assertThat(os.extrairEventos()).anySatisfy(e -> assertThat(e).isInstanceOf(OrcamentoRejeitado.class));

            os.entregar(T0.plusSeconds(900));
            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
        }

        @Test
        void aprovacaoERejeicaoExigemOrcamentoEnviado() {
            assertThatThrownBy(() -> os.aprovarOrcamento(T0)).isInstanceOf(RegraNegocioException.class);
            assertThatThrownBy(() -> os.rejeitarOrcamento(null, T0)).isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void naoPermitePularEtapas() {
            assertThatThrownBy(() -> os.entregar(T0))
                    .isInstanceOf(TransicaoStatusInvalidaException.class)
                    .hasMessageContaining("RECEBIDA -> ENTREGUE");
            assertThatThrownBy(() -> os.finalizar(T0)).isInstanceOf(RegraNegocioException.class);
            assertThatThrownBy(() -> os.registrarDiagnostico("x")).isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void diagnosticoNaoPodeSerIniciadoDuasVezes() {
            os.iniciarDiagnostico(T0);
            assertThatThrownBy(() -> os.iniciarDiagnostico(T0)).isInstanceOf(TransicaoStatusInvalidaException.class);
        }

        @Test
        void entregueEhEstadoFinal() {
            comItensAguardandoAprovacao();
            os.aprovarOrcamento(T0);
            os.finalizar(T0);
            os.entregar(T0);

            assertThat(StatusOrdemServico.ENTREGUE.proximosPermitidos()).isEmpty();
            assertThatThrownBy(() -> os.entregar(T0)).isInstanceOf(TransicaoStatusInvalidaException.class);
        }
    }

    @Test
    void reconstituicaoNaoGeraEventos() {
        OrdemServico copia = OrdemServico.reconstituir()
                .id(os.getId())
                .clienteId(clienteId)
                .veiculoId(veiculoId)
                .status(StatusOrdemServico.EM_EXECUCAO)
                .situacaoOrcamento(SituacaoOrcamento.APROVADO)
                .abertaEm(T0)
                .inicioExecucao(T0)
                .versao(3L)
                .build();

        assertThat(copia.extrairEventos()).isEmpty();
        assertThat(copia).isEqualTo(os).hasSameHashCodeAs(os);
        assertThat(copia.getVersao()).isEqualTo(3L);
        assertThat(copia.getClienteId()).isEqualTo(clienteId);
        assertThat(copia.getVeiculoId()).isEqualTo(veiculoId);
    }
}
