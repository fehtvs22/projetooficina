package br.com.oficina.application.servico;

import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.servico.ServicoRepository;
import br.com.oficina.domain.shared.Dinheiro;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso administrativos do catálogo de serviços (CRUD).
 */
public class GerenciarServicosUseCase {

    private final ServicoRepository servicos;

    public GerenciarServicosUseCase(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public Servico cadastrar(DadosServico dados) {
        return servicos.salvar(Servico.cadastrar(dados.nome(), dados.descricao(), Dinheiro.de(dados.preco()),
                dados.tempoEstimadoMinutos()));
    }

    public Servico atualizar(UUID id, DadosServico dados) {
        Servico servico = buscarPorId(id);
        servico.atualizar(dados.nome(), dados.descricao(), Dinheiro.de(dados.preco()), dados.tempoEstimadoMinutos());
        return servicos.salvar(servico);
    }

    public Servico buscarPorId(UUID id) {
        return servicos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    public List<Servico> listar() {
        return servicos.listar();
    }

    public void remover(UUID id) {
        buscarPorId(id);
        servicos.remover(id);
    }
}
