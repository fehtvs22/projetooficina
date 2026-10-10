package br.com.oficina.application.ordemservico;

import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.ordemservico.StatusOrdemServico;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: listagem e detalhamento de OS (oficina) e acompanhamento pelo cliente.
 */
public class ConsultarOrdensServicoUseCase {

    private final OrdemServicoRepository ordens;
    private final ClienteRepository clientes;
    private final OrdensServico ordensServico;

    public ConsultarOrdensServicoUseCase(OrdemServicoRepository ordens, ClienteRepository clientes,
                                         OrdensServico ordensServico) {
        this.ordens = ordens;
        this.clientes = clientes;
        this.ordensServico = ordensServico;
    }

    public OrdemServicoDetalhe buscarPorId(UUID id) {
        return ordensServico.detalhar(ordensServico.obter(id));
    }

    public List<OrdemServicoDetalhe> listar(StatusOrdemServico status) {
        return ordens.listar(status).stream().map(ordensServico::detalhar).toList();
    }

    public OrdemServicoDetalhe consultarPeloCliente(UUID id, String documento) {
        return ordensServico.detalhar(ordensServico.obterDoCliente(id, documento));
    }

    /**
     * Documento sem cadastro devolve lista vazia, como um cliente sem OS: a rota é pública e não deve
     * revelar quais CPFs/CNPJs são clientes da oficina.
     */
    public List<OrdemServicoDetalhe> listarDoCliente(String documento) {
        return clientes.buscarPorDocumento(Documento.de(documento))
                .map(cliente -> ordens.listarPorCliente(cliente.getId()).stream().map(ordensServico::detalhar).toList())
                .orElse(List.of());
    }
}
