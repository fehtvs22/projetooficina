package br.com.oficina.application.ordemservico;

import br.com.oficina.application.shared.PublicadorEventos;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.ordemservico.ItemPeca;
import br.com.oficina.domain.ordemservico.OrdemServico;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

/**
 * Caso de uso: aprovação ou rejeição do orçamento, pelo cliente (API pública) ou registrada pela oficina.
 * <p>
 * Na aprovação, a OS entra em execução e as peças/insumos são baixados do estoque na mesma transação:
 * se alguma peça não tiver saldo, nada é gravado.
 */
public class ResponderOrcamentoUseCase {

    private final OrdensServico ordensServico;
    private final PecaRepository pecas;
    private final PublicadorEventos publicador;
    private final Clock relogio;

    public ResponderOrcamentoUseCase(OrdensServico ordensServico, PecaRepository pecas, PublicadorEventos publicador,
                                     Clock relogio) {
        this.ordensServico = ordensServico;
        this.pecas = pecas;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public OrdemServicoDetalhe aprovar(UUID ordemId) {
        return aprovar(ordensServico.obter(ordemId));
    }

    public OrdemServicoDetalhe rejeitar(UUID ordemId, String motivo) {
        return rejeitar(ordensServico.obter(ordemId), motivo);
    }

    public OrdemServicoDetalhe aprovarPeloCliente(UUID ordemId, String documento) {
        return aprovar(ordensServico.obterDoCliente(ordemId, documento));
    }

    public OrdemServicoDetalhe rejeitarPeloCliente(UUID ordemId, String documento, String motivo) {
        return rejeitar(ordensServico.obterDoCliente(ordemId, documento), motivo);
    }

    private OrdemServicoDetalhe aprovar(OrdemServico os) {
        Instant agora = relogio.instant();
        os.aprovarOrcamento(agora);
        // Ordena por id para que transações concorrentes bloqueiem as peças sempre na mesma ordem (evita deadlock)
        os.getPecas().stream()
                .sorted(Comparator.comparing(ItemPeca::getPecaId))
                .forEach(item -> baixarEstoque(item, agora));
        return ordensServico.salvar(os);
    }

    private OrdemServicoDetalhe rejeitar(OrdemServico os, String motivo) {
        os.rejeitarOrcamento(motivo, relogio.instant());
        return ordensServico.salvar(os);
    }

    private void baixarEstoque(ItemPeca item, Instant agora) {
        Peca peca = pecas.buscarPorIdParaAtualizacao(item.getPecaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", item.getPecaId()));
        peca.baixar(item.getQuantidade(), agora);
        pecas.salvar(peca);
        publicador.publicar(peca.extrairEventos());
    }
}
