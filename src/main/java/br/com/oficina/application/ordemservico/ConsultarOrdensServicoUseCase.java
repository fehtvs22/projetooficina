package br.com.oficina.application.ordemservico;

import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
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

    public List<OrdemServicoDetalhe> listarDoCliente(String documento) {
        Documento doc = Documento.de(documento);
        Cliente cliente = clientes.buscarPorDocumento(doc)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", doc.formatado()));
        return ordens.listarPorCliente(cliente.getId()).stream().map(ordensServico::detalhar).toList();
    }
}
