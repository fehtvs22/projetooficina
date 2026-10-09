package br.com.oficina.application.ordemservico;

import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.shared.ValidacaoException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.domain.veiculo.VeiculoRepository;

import java.time.Clock;
import java.util.Optional;

/**
 * Caso de uso: abertura da Ordem de Serviço.
 * <ol>
 *   <li>Identifica o cliente pelo CPF/CNPJ;</li>
 *   <li>Localiza o veículo pela placa ou o cadastra (placa, marca, modelo, ano);</li>
 *   <li>Abre a OS como RECEBIDA e inclui os serviços/peças já solicitados.</li>
 * </ol>
 */
public class AbrirOrdemServicoUseCase {

    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;
    private final GerenciarItensOrdemServicoUseCase itens;
    private final OrdensServico ordensServico;
    private final Clock relogio;

    public AbrirOrdemServicoUseCase(ClienteRepository clientes, VeiculoRepository veiculos,
                                    GerenciarItensOrdemServicoUseCase itens, OrdensServico ordensServico,
                                    Clock relogio) {
        this.clientes = clientes;
        this.veiculos = veiculos;
        this.itens = itens;
        this.ordensServico = ordensServico;
        this.relogio = relogio;
    }

    public OrdemServicoDetalhe executar(AbrirOrdemServicoCommand comando) {
        Documento documento = Documento.de(comando.documentoCliente());
        Cliente cliente = clientes.buscarPorDocumento(documento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", documento.formatado()));
        Veiculo veiculo = obterOuCadastrarVeiculo(comando.veiculo(), cliente);

        OrdemServico os = OrdemServico.abrir(cliente.getId(), veiculo.getId(), comando.descricaoProblema(),
                relogio.instant());
        comando.servicosOuVazio().forEach(s -> itens.incluirServico(os, s.id(), s.quantidade()));
        comando.pecasOuVazio().forEach(p -> itens.incluirPeca(os, p.id(), p.quantidade()));
        return ordensServico.salvar(os);
    }

    private Veiculo obterOuCadastrarVeiculo(AbrirOrdemServicoCommand.Veiculo dados, Cliente cliente) {
        if (dados == null) {
            throw new ValidacaoException("Dados do veículo são obrigatórios");
        }
        Placa placa = Placa.de(dados.placa());
        Optional<Veiculo> existente = veiculos.buscarPorPlaca(placa);
        if (existente.isPresent()) {
            if (!existente.get().pertenceA(cliente.getId())) {
                throw new RegraNegocioException("O veículo " + placa.valor() + " está cadastrado para outro cliente");
            }
            return existente.get();
        }
        if (dados.ano() == null) {
            throw new ValidacaoException("Ano do veículo é obrigatório para cadastrar um novo veículo");
        }
        return veiculos.salvar(Veiculo.cadastrar(placa, dados.marca(), dados.modelo(), dados.ano(), cliente.getId()));
    }
}
