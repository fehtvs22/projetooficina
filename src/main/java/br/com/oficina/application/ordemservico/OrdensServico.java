package br.com.oficina.application.ordemservico;

import br.com.oficina.application.shared.PublicadorEventos;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.domain.veiculo.VeiculoRepository;

import java.util.UUID;

/**
 * Apoio compartilhado pelos casos de uso de OS: carregar, salvar publicando eventos e montar o detalhe.
 */
public class OrdensServico {

    private final OrdemServicoRepository ordens;
    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;
    private final PublicadorEventos publicador;

    public OrdensServico(OrdemServicoRepository ordens, ClienteRepository clientes, VeiculoRepository veiculos,
                         PublicadorEventos publicador) {
        this.ordens = ordens;
        this.clientes = clientes;
        this.veiculos = veiculos;
        this.publicador = publicador;
    }

    public OrdemServico obter(UUID id) {
        return ordens.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço", id));
    }

    /**
     * Obtém a OS garantindo que pertence ao cliente do documento informado. Para não revelar a existência
     * de OS de terceiros, a divergência é tratada como "não encontrada".
     */
    public OrdemServico obterDoCliente(UUID id, String documento) {
        Documento doc = Documento.de(documento);
        OrdemServico os = obter(id);
        Cliente cliente = clientes.buscarPorId(os.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço", id));
        if (!cliente.possuiDocumento(doc)) {
            throw new RecursoNaoEncontradoException("Ordem de serviço", id);
        }
        return os;
    }

    public OrdemServicoDetalhe salvar(OrdemServico os) {
        OrdemServico salva = ordens.salvar(os);
        publicador.publicar(os.extrairEventos());
        return detalhar(salva);
    }

    public OrdemServicoDetalhe detalhar(OrdemServico os) {
        Cliente cliente = clientes.buscarPorId(os.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", os.getClienteId()));
        Veiculo veiculo = veiculos.buscarPorId(os.getVeiculoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", os.getVeiculoId()));
        return new OrdemServicoDetalhe(os, cliente, veiculo);
    }
}
