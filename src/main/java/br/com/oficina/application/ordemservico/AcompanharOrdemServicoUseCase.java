package br.com.oficina.application.ordemservico;

import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.estoque.EstoqueInsuficienteException;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.ordemservico.ItemPeca;
import br.com.oficina.domain.ordemservico.OrdemServico;

import java.time.Clock;
import java.util.UUID;

/**
 * Caso de uso: avanço do fluxo da OS pela oficina (diagnóstico, envio do orçamento, finalização e entrega).
 * Cada ação altera o status automaticamente segundo as regras de transição do agregado.
 */
public class AcompanharOrdemServicoUseCase {

    private final OrdensServico ordensServico;
    private final PecaRepository pecas;
    private final Clock relogio;

    public AcompanharOrdemServicoUseCase(OrdensServico ordensServico, PecaRepository pecas, Clock relogio) {
        this.ordensServico = ordensServico;
        this.pecas = pecas;
        this.relogio = relogio;
    }

    public OrdemServicoDetalhe iniciarDiagnostico(UUID ordemId) {
        OrdemServico os = ordensServico.obter(ordemId);
        os.iniciarDiagnostico(relogio.instant());
        return ordensServico.salvar(os);
    }

    public OrdemServicoDetalhe registrarDiagnostico(UUID ordemId, String diagnostico) {
        OrdemServico os = ordensServico.obter(ordemId);
        os.registrarDiagnostico(diagnostico);
        return ordensServico.salvar(os);
    }

    /**
     * Gera o orçamento final e o disponibiliza ao cliente. Confere se há estoque para as peças,
     * evitando que o cliente aprove algo que não pode ser executado.
     */
    public OrdemServicoDetalhe enviarOrcamento(UUID ordemId) {
        OrdemServico os = ordensServico.obter(ordemId);
        for (ItemPeca item : os.getPecas()) {
            Peca peca = pecas.buscarPorId(item.getPecaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", item.getPecaId()));
            if (!peca.possuiDisponivel(item.getQuantidade())) {
                throw new EstoqueInsuficienteException(peca.getNome(), peca.getQuantidadeEstoque(), item.getQuantidade());
            }
        }
        os.enviarOrcamento(relogio.instant());
        return ordensServico.salvar(os);
    }

    public OrdemServicoDetalhe finalizar(UUID ordemId) {
        OrdemServico os = ordensServico.obter(ordemId);
        os.finalizar(relogio.instant());
        return ordensServico.salvar(os);
    }

    public OrdemServicoDetalhe entregar(UUID ordemId) {
        OrdemServico os = ordensServico.obter(ordemId);
        os.entregar(relogio.instant());
        return ordensServico.salvar(os);
    }
}
