package br.com.oficina.application.veiculo;

import br.com.oficina.application.shared.ConflitoException;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.domain.veiculo.VeiculoRepository;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso administrativos de veículos (CRUD).
 */
public class GerenciarVeiculosUseCase {

    private final VeiculoRepository veiculos;
    private final ClienteRepository clientes;
    private final OrdemServicoRepository ordens;

    public GerenciarVeiculosUseCase(VeiculoRepository veiculos, ClienteRepository clientes,
                                    OrdemServicoRepository ordens) {
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.ordens = ordens;
    }

    public Veiculo cadastrar(DadosVeiculo dados) {
        Placa placa = Placa.de(dados.placa());
        exigirCliente(dados.clienteId());
        if (veiculos.buscarPorPlaca(placa).isPresent()) {
            throw new ConflitoException("Já existe veículo com a placa " + placa.valor());
        }
        return veiculos.salvar(Veiculo.cadastrar(placa, dados.marca(), dados.modelo(), dados.ano(), dados.clienteId()));
    }

    public Veiculo atualizar(UUID id, DadosVeiculo dados) {
        Veiculo veiculo = buscarPorId(id);
        if (dados.placa() != null && !veiculo.getPlaca().equals(Placa.de(dados.placa()))) {
            throw new RegraNegocioException("A placa do veículo não pode ser alterada");
        }
        exigirCliente(dados.clienteId());
        veiculo.atualizar(dados.marca(), dados.modelo(), dados.ano(), dados.clienteId());
        return veiculos.salvar(veiculo);
    }

    public Veiculo buscarPorId(UUID id) {
        return veiculos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
    }

    public List<Veiculo> listar(UUID clienteId) {
        return clienteId == null ? veiculos.listar() : veiculos.listarPorCliente(clienteId);
    }

    public void remover(UUID id) {
        buscarPorId(id);
        if (ordens.existeParaVeiculo(id)) {
            throw new RegraNegocioException("Veículo possui ordens de serviço e não pode ser removido");
        }
        veiculos.remover(id);
    }

    private void exigirCliente(UUID clienteId) {
        if (clienteId == null || clientes.buscarPorId(clienteId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente", clienteId);
        }
    }
}
