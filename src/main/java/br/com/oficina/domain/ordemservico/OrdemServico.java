package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.ordemservico.evento.OrcamentoAprovado;
import br.com.oficina.domain.ordemservico.evento.OrcamentoEnviado;
import br.com.oficina.domain.ordemservico.evento.OrcamentoRejeitado;
import br.com.oficina.domain.ordemservico.evento.OrdemServicoAberta;
import br.com.oficina.domain.ordemservico.evento.OrdemServicoFinalizada;
import br.com.oficina.domain.ordemservico.evento.StatusOrdemServicoAlterado;
import br.com.oficina.domain.ordemservico.evento.VeiculoEntregue;
import br.com.oficina.domain.shared.AgregadoRaiz;
import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.RegraNegocioException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Raiz do agregado Ordem de Serviço (OS).
 * <p>
 * Concentra as regras do atendimento: inclusão de serviços e peças, geração do orçamento,
 * aprovação/rejeição e o ciclo de vida de status. Cliente, veículo, serviços e peças são
 * referenciados por identificador; preços são congelados nos itens no momento da inclusão.
 */
public class OrdemServico extends AgregadoRaiz {

    private final UUID id;
    private final UUID clienteId;
    private final UUID veiculoId;
    private final String descricaoProblema;
    private String diagnostico;
    private StatusOrdemServico status;
    private final List<ItemServico> servicos;
    private final List<ItemPeca> pecas;
    private final List<HistoricoStatus> historico;
    private SituacaoOrcamento situacaoOrcamento;
    private Instant orcamentoEnviadoEm;
    private Instant orcamentoRespondidoEm;
    private String motivoRejeicao;
    private final Instant abertaEm;
    private Instant inicioExecucao;
    private Instant fimExecucao;
    private Instant entregueEm;
    private final Long versao;

    private OrdemServico(Builder b) {
        this.id = Assercoes.obrigatorio(b.id, "Id da ordem de serviço");
        this.clienteId = Assercoes.obrigatorio(b.clienteId, "Cliente");
        this.veiculoId = Assercoes.obrigatorio(b.veiculoId, "Veículo");
        this.descricaoProblema = Assercoes.textoOpcional(b.descricaoProblema);
        this.diagnostico = Assercoes.textoOpcional(b.diagnostico);
        this.status = Assercoes.obrigatorio(b.status, "Status");
        this.servicos = new ArrayList<>(b.servicos);
        this.pecas = new ArrayList<>(b.pecas);
        this.historico = new ArrayList<>(b.historico);
        this.situacaoOrcamento = Assercoes.obrigatorio(b.situacaoOrcamento, "Situação do orçamento");
        this.orcamentoEnviadoEm = b.orcamentoEnviadoEm;
        this.orcamentoRespondidoEm = b.orcamentoRespondidoEm;
        this.motivoRejeicao = b.motivoRejeicao;
        this.abertaEm = Assercoes.obrigatorio(b.abertaEm, "Data de abertura");
        this.inicioExecucao = b.inicioExecucao;
        this.fimExecucao = b.fimExecucao;
        this.entregueEm = b.entregueEm;
        this.versao = b.versao;
    }

    /**
     * Abre uma nova OS no status RECEBIDA.
     */
    public static OrdemServico abrir(UUID clienteId, UUID veiculoId, String descricaoProblema, Instant agora) {
        OrdemServico os = new Builder()
                .id(UUID.randomUUID())
                .clienteId(clienteId)
                .veiculoId(veiculoId)
                .descricaoProblema(descricaoProblema)
                .status(StatusOrdemServico.RECEBIDA)
                .situacaoOrcamento(SituacaoOrcamento.EM_ELABORACAO)
                .abertaEm(agora)
                .historico(List.of(HistoricoStatus.de(StatusOrdemServico.RECEBIDA, agora)))
                .build();
        os.registrarEvento(new OrdemServicoAberta(os.id, clienteId, veiculoId, agora));
        return os;
    }

    public static Builder reconstituir() {
        return new Builder();
    }

    // ---------- Diagnóstico e itens ----------

    public void iniciarDiagnostico(Instant agora) {
        alterarStatus(StatusOrdemServico.EM_DIAGNOSTICO, agora);
    }

    public void registrarDiagnostico(String diagnostico) {
        exigirStatus(StatusOrdemServico.EM_DIAGNOSTICO, "registrar o diagnóstico");
        this.diagnostico = Assercoes.textoObrigatorio(diagnostico, "Diagnóstico");
    }

    public ItemServico adicionarServico(UUID servicoId, String descricao, Dinheiro preco, int quantidade) {
        exigirItensEditaveis();
        Optional<ItemServico> existente = servicos.stream()
                .filter(i -> i.getServicoId().equals(servicoId))
                .findFirst();
        if (existente.isPresent()) {
            existente.get().acrescentar(quantidade);
            return existente.get();
        }
        ItemServico item = new ItemServico(UUID.randomUUID(), servicoId, descricao, preco, quantidade);
        servicos.add(item);
        return item;
    }

    public void removerServico(UUID itemId) {
        exigirItensEditaveis();
        if (!servicos.removeIf(i -> i.getId().equals(itemId))) {
            throw new RegraNegocioException("Serviço " + itemId + " não pertence à ordem de serviço");
        }
    }

    public ItemPeca adicionarPeca(UUID pecaId, String descricao, Dinheiro preco, int quantidade) {
        exigirItensEditaveis();
        Optional<ItemPeca> existente = pecas.stream()
                .filter(i -> i.getPecaId().equals(pecaId))
                .findFirst();
        if (existente.isPresent()) {
            existente.get().acrescentar(quantidade);
            return existente.get();
        }
        ItemPeca item = new ItemPeca(UUID.randomUUID(), pecaId, descricao, preco, quantidade);
        pecas.add(item);
        return item;
    }

    public void removerPeca(UUID itemId) {
        exigirItensEditaveis();
        if (!pecas.removeIf(i -> i.getId().equals(itemId))) {
            throw new RegraNegocioException("Peça " + itemId + " não pertence à ordem de serviço");
        }
    }

    public int quantidadeDaPeca(UUID pecaId) {
        return pecas.stream().filter(i -> i.getPecaId().equals(pecaId)).mapToInt(ItemPeca::getQuantidade).sum();
    }

    // ---------- Orçamento ----------

    /**
     * Orçamento gerado automaticamente a partir dos itens incluídos.
     */
    public Orcamento getOrcamento() {
        Dinheiro valorServicos = servicos.stream().map(ItemServico::subtotal).reduce(Dinheiro.ZERO, Dinheiro::somar);
        Dinheiro valorPecas = pecas.stream().map(ItemPeca::subtotal).reduce(Dinheiro.ZERO, Dinheiro::somar);
        return new Orcamento(valorServicos, valorPecas, valorServicos.somar(valorPecas), situacaoOrcamento,
                orcamentoEnviadoEm, orcamentoRespondidoEm, motivoRejeicao);
    }

    public void enviarOrcamento(Instant agora) {
        exigirStatus(StatusOrdemServico.EM_DIAGNOSTICO, "enviar o orçamento");
        if (servicos.isEmpty() && pecas.isEmpty()) {
            throw new RegraNegocioException("O orçamento precisa de ao menos um serviço ou peça");
        }
        alterarStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, agora);
        situacaoOrcamento = SituacaoOrcamento.AGUARDANDO_APROVACAO;
        orcamentoEnviadoEm = agora;
        registrarEvento(new OrcamentoEnviado(id, clienteId, getOrcamento().valorTotal(), agora));
    }

    /**
     * Aprovação do orçamento: a execução começa imediatamente (a baixa de estoque é coordenada pela aplicação).
     */
    public void aprovarOrcamento(Instant agora) {
        exigirStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "aprovar o orçamento");
        situacaoOrcamento = SituacaoOrcamento.APROVADO;
        orcamentoRespondidoEm = agora;
        registrarEvento(new OrcamentoAprovado(id, clienteId, getOrcamento().valorTotal(), agora));
        alterarStatus(StatusOrdemServico.EM_EXECUCAO, agora);
        inicioExecucao = agora;
    }

    /**
     * Rejeição do orçamento: nenhum serviço é executado e a OS é finalizada para devolução do veículo.
     */
    public void rejeitarOrcamento(String motivo, Instant agora) {
        exigirStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "rejeitar o orçamento");
        situacaoOrcamento = SituacaoOrcamento.REJEITADO;
        orcamentoRespondidoEm = agora;
        motivoRejeicao = Assercoes.textoOpcional(motivo);
        registrarEvento(new OrcamentoRejeitado(id, clienteId, motivoRejeicao, agora));
        alterarStatus(StatusOrdemServico.FINALIZADA, agora);
    }

    // ---------- Execução e entrega ----------

    public void finalizar(Instant agora) {
        exigirStatus(StatusOrdemServico.EM_EXECUCAO, "finalizar a execução");
        alterarStatus(StatusOrdemServico.FINALIZADA, agora);
        fimExecucao = agora;
        registrarEvento(new OrdemServicoFinalizada(id, clienteId, agora));
    }

    public void entregar(Instant agora) {
        alterarStatus(StatusOrdemServico.ENTREGUE, agora);
        entregueEm = agora;
        registrarEvento(new VeiculoEntregue(id, clienteId, veiculoId, agora));
    }

    /**
     * Tempo efetivo de execução (aprovação até finalização), quando a OS foi executada.
     */
    public Optional<Duration> duracaoExecucao() {
        if (inicioExecucao == null || fimExecucao == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(inicioExecucao, fimExecucao));
    }

    public boolean pertenceAoCliente(UUID clienteId) {
        return this.clienteId.equals(clienteId);
    }

    // ---------- Regras internas ----------

    private void alterarStatus(StatusOrdemServico destino, Instant agora) {
        if (!status.podeTransicionarPara(destino)) {
            throw new TransicaoStatusInvalidaException(status, destino);
        }
        StatusOrdemServico anterior = status;
        status = destino;
        historico.add(HistoricoStatus.de(destino, agora));
        registrarEvento(new StatusOrdemServicoAlterado(id, clienteId, anterior, destino, agora));
    }

    private void exigirStatus(StatusOrdemServico esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException("Não é possível " + acao + " com a OS no status " + status
                    + " (esperado: " + esperado + ")");
        }
    }

    private void exigirItensEditaveis() {
        if (!status.permiteAlterarItens()) {
            throw new RegraNegocioException("Itens só podem ser alterados nos status RECEBIDA ou EM_DIAGNOSTICO (atual: "
                    + status + ")");
        }
    }

    // ---------- Getters ----------

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public String getDescricaoProblema() {
        return descricaoProblema;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public List<ItemServico> getServicos() {
        return Collections.unmodifiableList(servicos);
    }

    public List<ItemPeca> getPecas() {
        return Collections.unmodifiableList(pecas);
    }

    public List<HistoricoStatus> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    public Instant getAbertaEm() {
        return abertaEm;
    }

    public Instant getInicioExecucao() {
        return inicioExecucao;
    }

    public Instant getFimExecucao() {
        return fimExecucao;
    }

    public Instant getEntregueEm() {
        return entregueEm;
    }

    public Long getVersao() {
        return versao;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OrdemServico outra && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Reconstitui o agregado a partir do armazenamento, sem disparar regras nem eventos.
     */
    public static final class Builder {
        private UUID id;
        private UUID clienteId;
        private UUID veiculoId;
        private String descricaoProblema;
        private String diagnostico;
        private StatusOrdemServico status;
        private List<ItemServico> servicos = List.of();
        private List<ItemPeca> pecas = List.of();
        private List<HistoricoStatus> historico = List.of();
        private SituacaoOrcamento situacaoOrcamento;
        private Instant orcamentoEnviadoEm;
        private Instant orcamentoRespondidoEm;
        private String motivoRejeicao;
        private Instant abertaEm;
        private Instant inicioExecucao;
        private Instant fimExecucao;
        private Instant entregueEm;
        private Long versao;

        private Builder() {
        }

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder clienteId(UUID clienteId) { this.clienteId = clienteId; return this; }
        public Builder veiculoId(UUID veiculoId) { this.veiculoId = veiculoId; return this; }
        public Builder descricaoProblema(String descricaoProblema) { this.descricaoProblema = descricaoProblema; return this; }
        public Builder diagnostico(String diagnostico) { this.diagnostico = diagnostico; return this; }
        public Builder status(StatusOrdemServico status) { this.status = status; return this; }
        public Builder servicos(List<ItemServico> servicos) { this.servicos = servicos; return this; }
        public Builder pecas(List<ItemPeca> pecas) { this.pecas = pecas; return this; }
        public Builder historico(List<HistoricoStatus> historico) { this.historico = historico; return this; }
        public Builder situacaoOrcamento(SituacaoOrcamento situacao) { this.situacaoOrcamento = situacao; return this; }
        public Builder orcamentoEnviadoEm(Instant instante) { this.orcamentoEnviadoEm = instante; return this; }
        public Builder orcamentoRespondidoEm(Instant instante) { this.orcamentoRespondidoEm = instante; return this; }
        public Builder motivoRejeicao(String motivo) { this.motivoRejeicao = motivo; return this; }
        public Builder abertaEm(Instant instante) { this.abertaEm = instante; return this; }
        public Builder inicioExecucao(Instant instante) { this.inicioExecucao = instante; return this; }
        public Builder fimExecucao(Instant instante) { this.fimExecucao = instante; return this; }
        public Builder entregueEm(Instant instante) { this.entregueEm = instante; return this; }
        public Builder versao(Long versao) { this.versao = versao; return this; }

        public OrdemServico build() {
            return new OrdemServico(this);
        }
    }
}
