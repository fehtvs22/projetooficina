package br.com.oficina.application.cliente;

import br.com.oficina.application.shared.ConflitoException;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.shared.Email;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.VeiculoRepository;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso administrativos de clientes (CRUD).
 */
public class GerenciarClientesUseCase {

    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;

    public GerenciarClientesUseCase(ClienteRepository clientes, VeiculoRepository veiculos) {
        this.clientes = clientes;
        this.veiculos = veiculos;
    }

    public Cliente cadastrar(DadosCliente dados) {
        Documento documento = Documento.de(dados.documento());
        if (clientes.buscarPorDocumento(documento).isPresent()) {
            throw new ConflitoException("Já existe cliente com o documento " + documento.formatado());
        }
        Cliente cliente = Cliente.cadastrar(dados.nome(), documento, Email.deOpcional(dados.email()), dados.telefone());
        return clientes.salvar(cliente);
    }

    public Cliente atualizar(UUID id, DadosCliente dados) {
        Cliente cliente = buscarPorId(id);
        if (dados.documento() != null && !cliente.possuiDocumento(Documento.de(dados.documento()))) {
            throw new RegraNegocioException("O documento do cliente não pode ser alterado");
        }
        cliente.atualizarContato(dados.nome(), Email.deOpcional(dados.email()), dados.telefone());
        return clientes.salvar(cliente);
    }

    public Cliente buscarPorId(UUID id) {
        return clientes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    public Cliente buscarPorDocumento(String documento) {
        Documento doc = Documento.de(documento);
        return clientes.buscarPorDocumento(doc)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", doc.formatado()));
    }

    public List<Cliente> listar() {
        return clientes.listar();
    }

    public void remover(UUID id) {
        buscarPorId(id);
        if (!veiculos.listarPorCliente(id).isEmpty()) {
            throw new RegraNegocioException("Cliente possui veículos cadastrados e não pode ser removido");
        }
        clientes.remover(id);
    }
}
