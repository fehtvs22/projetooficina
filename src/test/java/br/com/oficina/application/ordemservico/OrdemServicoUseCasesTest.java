package br.com.oficina.application.ordemservico;

import br.com.oficina.application.fakes.EmMemoria;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.estoque.EstoqueAbaixoDoMinimo;
import br.com.oficina.domain.estoque.EstoqueInsuficienteException;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.TipoPeca;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.SituacaoOrcamento;
import br.com.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.oficina.domain.ordemservico.TempoMedioExecucao;
import br.com.oficina.domain.ordemservico.evento.OrcamentoEnviado;
import br.com.oficina.domain.ordemservico.evento.OrdemServicoAberta;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.shared.ValidacaoException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdemServicoUseCasesTest {

    private static final String CPF = "529.982.247-25";

    /**
     * Relógio controlável para medir o tempo de execução.
     */
    static final class RelogioAjustavel extends Clock {
        Instant agora = Instant.parse("2026-05-04T12:00:00Z");

        void avancar(Duration d) {
            agora = agora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final EmMemoria.Clientes clientes = new EmMemoria.Clientes();
    private final EmMemoria.Veiculos veiculos = new EmMemoria.Veiculos();
    private final EmMemoria.Servicos servicos = new EmMemoria.Servicos();
    private final EmMemoria.Pecas pecas = new EmMemoria.Pecas();
    private final EmMemoria.Ordens ordens = new EmMemoria.Ordens();
    private final EmMemoria.Eventos eventos = new EmMemoria.Eventos();
    private final RelogioAjustavel relogio = new RelogioAjustavel();

    private final OrdensServico apoio = new OrdensServico(ordens, clientes, veiculos, eventos);
    private final GerenciarItensOrdemServicoUseCase itens = new GerenciarItensOrdemServicoUseCase(servicos, pecas, apoio);
    private final AbrirOrdemServicoUseCase abrir = new AbrirOrdemServicoUseCase(clientes, veiculos, itens, apoio, relogio);
    private final AcompanharOrdemServicoUseCase acompanhar = new AcompanharOrdemServicoUseCase(apoio, pecas, relogio);
    private final ResponderOrcamentoUseCase responder = new ResponderOrcamentoUseCase(apoio, pecas, eventos, relogio);
    private final ConsultarOrdensServicoUseCase consultar = new ConsultarOrdensServicoUseCase(ordens, clientes, apoio);
    private final CalcularTempoMedioExecucaoUseCase tempoMedio = new CalcularTempoMedioExecucaoUseCase(ordens);

    private Cliente maria;
    private Servico trocaOleo;
    private Peca oleo;
    private Peca filtro;

    @BeforeEach
    void cenario() {
        maria = clientes.salvar(Cliente.cadastrar("Maria", Documento.de(CPF), null, null));
        trocaOleo = servicos.salvar(Servico.cadastrar("Troca de óleo", null, Dinheiro.de("120"), 40));
        oleo = pecas.salvar(Peca.cadastrar("OLEO", "Óleo 5W30", null, TipoPeca.INSUMO, Dinheiro.de("45.90"), 10, 5));
        filtro = pecas.salvar(Peca.cadastrar("FILTRO", "Filtro", null, TipoPeca.PECA, Dinheiro.de("38.50"), 2, 2));
    }

    private AbrirOrdemServicoCommand comando(List<ItemSolicitado> servicosSolicitados, List<ItemSolicitado> pecasSolicitadas) {
        return new AbrirOrdemServicoCommand(CPF, new AbrirOrdemServicoCommand.Veiculo("BRA2E19", "VW", "Gol", 2020),
                "Revisão", servicosSolicitados, pecasSolicitadas);
    }

    private OrdemServicoDetalhe abrirComItens() {
        return abrir.executar(comando(List.of(new ItemSolicitado(trocaOleo.getId(), 1)),
                List.of(new ItemSolicitado(oleo.getId(), 4), new ItemSolicitado(filtro.getId(), 1))));
    }

    private UUID aguardandoAprovacao() {
        UUID id = abrirComItens().ordemServico().getId();
        acompanhar.iniciarDiagnostico(id);
        acompanhar.enviarOrcamento(id);
        return id;
    }

    @Nested
    class Abertura {

        @Test
        void abreOsCadastrandoVeiculoEGerandoOrcamento() {
            OrdemServicoDetalhe detalhe = abrirComItens();
            OrdemServico os = detalhe.ordemServico();

            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
            assertThat(detalhe.cliente()).isEqualTo(maria);
            assertThat(detalhe.veiculo().getPlaca().valor()).isEqualTo("BRA2E19");
            assertThat(veiculos.listar()).hasSize(1);
            assertThat(os.getOrcamento().valorTotal().valor()).isEqualByComparingTo("342.10");
            assertThat(ordens.buscarPorId(os.getId())).isPresent();
            assertThat(eventos.doTipo(OrdemServicoAberta.class)).hasSize(1);
        }

        @Test
        void reutilizaVeiculoJaCadastradoDoCliente() {
            Veiculo gol = veiculos.salvar(Veiculo.cadastrar(Placa.de("BRA2E19"), "VW", "Gol", 2020, maria.getId()));

            OrdemServicoDetalhe detalhe = abrir.executar(new AbrirOrdemServicoCommand(CPF,
                    new AbrirOrdemServicoCommand.Veiculo("BRA-2E19", null, null, null), null, null, null));

            assertThat(detalhe.veiculo()).isEqualTo(gol);
            assertThat(detalhe.ordemServico().getServicos()).isEmpty();
        }

        @Test
        void recusaVeiculoDeOutroCliente() {
            veiculos.salvar(Veiculo.cadastrar(Placa.de("BRA2E19"), "VW", "Gol", 2020, UUID.randomUUID()));

            assertThatThrownBy(() -> abrir.executar(comando(null, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("outro cliente");
        }

        @Test
        void exigeClienteCadastradoEVeiculoCompleto() {
            assertThatThrownBy(() -> abrir.executar(new AbrirOrdemServicoCommand("11.222.333/0001-81",
                    new AbrirOrdemServicoCommand.Veiculo("BRA2E19", "VW", "Gol", 2020), null, null, null)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            assertThatThrownBy(() -> abrir.executar(new AbrirOrdemServicoCommand(CPF, null, null, null, null)))
                    .isInstanceOf(ValidacaoException.class);
            assertThatThrownBy(() -> abrir.executar(new AbrirOrdemServicoCommand(CPF,
                    new AbrirOrdemServicoCommand.Veiculo("BRA2E19", "VW", "Gol", null), null, null, null)))
                    .isInstanceOf(ValidacaoException.class)
                    .hasMessageContaining("Ano");
        }

        @Test
        void validaEstoqueEItensExistentes() {
            assertThatThrownBy(() -> abrir.executar(comando(null, List.of(new ItemSolicitado(filtro.getId(), 3)))))
                    .isInstanceOf(EstoqueInsuficienteException.class);
            assertThatThrownBy(() -> abrir.executar(comando(List.of(new ItemSolicitado(UUID.randomUUID(), 1)), null)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            assertThatThrownBy(() -> abrir.executar(comando(null, List.of(new ItemSolicitado(UUID.randomUUID(), 1)))))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    class Itens {

        @Test
        void incluiERemoveItens() {
            UUID id = abrir.executar(comando(null, null)).ordemServico().getId();

            OrdemServico os = itens.adicionarServico(id, trocaOleo.getId(), 1).ordemServico();
            os = itens.adicionarPeca(id, oleo.getId(), 2).ordemServico();
            assertThat(os.getOrcamento().valorTotal().valor()).isEqualByComparingTo("211.80");

            os = itens.removerServico(id, os.getServicos().getFirst().getId()).ordemServico();
            os = itens.removerPeca(id, os.getPecas().getFirst().getId()).ordemServico();
            assertThat(os.getOrcamento().valorTotal()).isEqualTo(Dinheiro.ZERO);
        }

        @Test
        void quantidadeAcumuladaRespeitaEstoque() {
            UUID id = abrir.executar(comando(null, List.of(new ItemSolicitado(filtro.getId(), 2)))).ordemServico().getId();

            assertThatThrownBy(() -> itens.adicionarPeca(id, filtro.getId(), 1))
                    .isInstanceOf(EstoqueInsuficienteException.class)
                    .hasMessageContaining("solicitado 3");
        }
    }

    @Nested
    class Fluxo {

        @Test
        void envioConfereEstoqueEPublicaOrcamento() {
            UUID id = aguardandoAprovacao();

            assertThat(consultar.buscarPorId(id).ordemServico().getStatus())
                    .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
            assertThat(eventos.doTipo(OrcamentoEnviado.class)).singleElement()
                    .satisfies(e -> assertThat(e.valorTotal().valor()).isEqualByComparingTo("342.10"));
        }

        @Test
        void envioFalhaSePecaFicouSemEstoque() {
            UUID id = abrirComItens().ordemServico().getId();
            acompanhar.iniciarDiagnostico(id);
            filtro.baixar(2, Instant.now());

            assertThatThrownBy(() -> acompanhar.enviarOrcamento(id)).isInstanceOf(EstoqueInsuficienteException.class);
        }

        @Test
        void envioFalhaSePecaFoiRemovidaDoCatalogo() {
            UUID id = abrirComItens().ordemServico().getId();
            acompanhar.iniciarDiagnostico(id);
            pecas.remover(filtro.getId());

            assertThatThrownBy(() -> acompanhar.enviarOrcamento(id)).isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        void aprovacaoPeloClienteBaixaEstoqueEIniciaExecucao() {
            UUID id = aguardandoAprovacao();

            OrdemServico os = responder.aprovarPeloCliente(id, "52998224725").ordemServico();

            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
            assertThat(os.getOrcamento().situacao()).isEqualTo(SituacaoOrcamento.APROVADO);
            assertThat(pecas.buscarPorId(oleo.getId()).orElseThrow().getQuantidadeEstoque()).isEqualTo(6);
            assertThat(pecas.buscarPorId(filtro.getId()).orElseThrow().getQuantidadeEstoque()).isEqualTo(1);
            assertThat(pecas.bloqueios).isEqualTo(2);
            assertThat(eventos.doTipo(EstoqueAbaixoDoMinimo.class)).hasSize(1);
        }

        @Test
        void aprovacaoPelaOficinaEExecucaoCompleta() {
            UUID id = aguardandoAprovacao();
            responder.aprovar(id);
            relogio.avancar(Duration.ofMinutes(50));
            acompanhar.finalizar(id);
            OrdemServico os = acompanhar.entregar(id).ordemServico();

            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
            TempoMedioExecucao tempo = tempoMedio.calcular();
            assertThat(tempo.ordensConsideradas()).isEqualTo(1);
            assertThat(tempo.mediaGeralMinutos()).isEqualTo(50.0);
        }

        @Test
        void rejeicaoNaoMexeNoEstoque() {
            UUID id = aguardandoAprovacao();

            OrdemServico os = responder.rejeitarPeloCliente(id, CPF, "Sem dinheiro").ordemServico();
            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
            assertThat(os.getOrcamento().motivoRejeicao()).isEqualTo("Sem dinheiro");
            assertThat(pecas.buscarPorId(oleo.getId()).orElseThrow().getQuantidadeEstoque()).isEqualTo(10);

            UUID outra = abrir.executar(new AbrirOrdemServicoCommand(CPF,
                    new AbrirOrdemServicoCommand.Veiculo("BRA2E19", null, null, null), null,
                    List.of(new ItemSolicitado(trocaOleo.getId(), 1)), null)).ordemServico().getId();
            acompanhar.iniciarDiagnostico(outra);
            acompanhar.enviarOrcamento(outra);
            assertThat(responder.rejeitar(outra, null).ordemServico().getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
        }

        @Test
        void clienteSoAcessaAsProprias() {
            UUID id = aguardandoAprovacao();
            clientes.salvar(Cliente.cadastrar("Empresa", Documento.de("11.222.333/0001-81"), null, null));

            assertThatThrownBy(() -> responder.aprovarPeloCliente(id, "11.222.333/0001-81"))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            assertThatThrownBy(() -> consultar.consultarPeloCliente(id, "11.222.333/0001-81"))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            assertThat(consultar.consultarPeloCliente(id, CPF).ordemServico().getId()).isEqualTo(id);
        }

        @Test
        void registraDiagnostico() {
            UUID id = abrirComItens().ordemServico().getId();
            acompanhar.iniciarDiagnostico(id);

            assertThat(acompanhar.registrarDiagnostico(id, "Filtro saturado").ordemServico().getDiagnostico())
                    .isEqualTo("Filtro saturado");
        }
    }

    @Nested
    class Consultas {

        @Test
        void listaPorStatusEPorCliente() {
            UUID a = abrirComItens().ordemServico().getId();
            UUID b = abrir.executar(comando(null, null)).ordemServico().getId();
            acompanhar.iniciarDiagnostico(b);

            assertThat(consultar.listar(null)).hasSize(2);
            assertThat(consultar.listar(StatusOrdemServico.RECEBIDA)).extracting(d -> d.ordemServico().getId())
                    .containsExactly(a);
            assertThat(consultar.listarDoCliente(CPF)).hasSize(2);
            assertThat(consultar.listarDoCliente("11.222.333/0001-81")).isEmpty();
        }

        @Test
        void ordemInexistente() {
            assertThatThrownBy(() -> consultar.buscarPorId(UUID.randomUUID()))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        void detalheExigeClienteEVeiculoExistentes() {
            OrdemServico orfa = ordens.salvar(OrdemServico.abrir(UUID.randomUUID(), UUID.randomUUID(), null, Instant.now()));
            assertThatThrownBy(() -> consultar.buscarPorId(orfa.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
            assertThatThrownBy(() -> consultar.consultarPeloCliente(orfa.getId(), CPF))
                    .isInstanceOf(RecursoNaoEncontradoException.class);

            OrdemServico semVeiculo = ordens.salvar(OrdemServico.abrir(maria.getId(), UUID.randomUUID(), null, Instant.now()));
            assertThatThrownBy(() -> consultar.buscarPorId(semVeiculo.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Veículo");
        }
    }
}
