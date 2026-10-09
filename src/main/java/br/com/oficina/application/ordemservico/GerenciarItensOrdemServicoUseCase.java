package br.com.oficina.application.ordemservico;

import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.estoque.EstoqueInsuficienteException;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.servico.ServicoRepository;

import java.util.UUID;

/**
 * Caso de uso: inclusão/remoção de serviços e peças/insumos na OS. O orçamento é recalculado
 * automaticamente pelo agregado a cada alteração.
 */
public class GerenciarItensOrdemServicoUseCase {

    private final ServicoRepository servicos;
    private final PecaRepository pecas;
    private final OrdensServico ordensServico;

    public GerenciarItensOrdemServicoUseCase(ServicoRepository servicos, PecaRepository pecas,
                                             OrdensServico ordensServico) {
        this.servicos = servicos;
        this.pecas = pecas;
        this.ordensServico = ordensServico;
    }

    public OrdemServicoDetalhe adicionarServico(UUID ordemId, UUID servicoId, int quantidade) {
        OrdemServico os = ordensServico.obter(ordemId);
        incluirServico(os, servicoId, quantidade);
        return ordensServico.salvar(os);
    }

    public OrdemServicoDetalhe removerServico(UUID ordemId, UUID itemId) {
        OrdemServico os = ordensServico.obter(ordemId);
        os.removerServico(itemId);
        return ordensServico.salvar(os);
    }

    public OrdemServicoDetalhe adicionarPeca(UUID ordemId, UUID pecaId, int quantidade) {
        OrdemServico os = ordensServico.obter(ordemId);
        incluirPeca(os, pecaId, quantidade);
        return ordensServico.salvar(os);
    }

    public OrdemServicoDetalhe removerPeca(UUID ordemId, UUID itemId) {
        OrdemServico os = ordensServico.obter(ordemId);
        os.removerPeca(itemId);
        return ordensServico.salvar(os);
    }

    public void incluirServico(OrdemServico os, UUID servicoId, int quantidade) {
        Servico servico = servicos.buscarPorId(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", servicoId));
        os.adicionarServico(servico.getId(), servico.getNome(), servico.getPreco(), quantidade);
    }

    public void incluirPeca(OrdemServico os, UUID pecaId, int quantidade) {
        Peca peca = pecas.buscarPorId(pecaId).orElseThrow(() -> new RecursoNaoEncontradoException("Peça", pecaId));
        int totalNaOrdem = os.quantidadeDaPeca(pecaId) + quantidade;
        if (!peca.possuiDisponivel(totalNaOrdem)) {
            throw new EstoqueInsuficienteException(peca.getNome(), peca.getQuantidadeEstoque(), totalNaOrdem);
        }
        os.adicionarPeca(peca.getId(), peca.getNome(), peca.getPreco(), quantidade);
    }
}
