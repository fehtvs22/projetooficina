package br.com.oficina.application.estoque;

import br.com.oficina.application.shared.ConflitoException;
import br.com.oficina.application.shared.PublicadorEventos;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.shared.Dinheiro;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso de peças e insumos: CRUD e movimentação de estoque.
 */
public class GerenciarPecasUseCase {

    private final PecaRepository pecas;
    private final PublicadorEventos publicador;
    private final Clock relogio;

    public GerenciarPecasUseCase(PecaRepository pecas, PublicadorEventos publicador, Clock relogio) {
        this.pecas = pecas;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public Peca cadastrar(DadosPeca dados) {
        String codigo = dados.codigo() == null ? null : dados.codigo().trim().toUpperCase();
        if (codigo != null && pecas.buscarPorCodigo(codigo).isPresent()) {
            throw new ConflitoException("Já existe peça com o código " + codigo);
        }
        Peca peca = Peca.cadastrar(codigo, dados.nome(), dados.descricao(), dados.tipo(), Dinheiro.de(dados.preco()),
                dados.quantidadeInicial(), dados.estoqueMinimo());
        return pecas.salvar(peca);
    }

    public Peca atualizar(UUID id, DadosPeca dados) {
        Peca peca = buscarPorId(id);
        String codigo = dados.codigo() == null ? null : dados.codigo().trim().toUpperCase();
        pecas.buscarPorCodigo(codigo == null ? "" : codigo)
                .filter(outra -> !outra.getId().equals(id))
                .ifPresent(outra -> {
                    throw new ConflitoException("Já existe peça com o código " + outra.getCodigo());
                });
        peca.atualizar(codigo, dados.nome(), dados.descricao(), dados.tipo(), Dinheiro.de(dados.preco()),
                dados.estoqueMinimo());
        return pecas.salvar(peca);
    }

    public Peca buscarPorId(UUID id) {
        return pecas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
    }

    public List<Peca> listar(boolean somenteAbaixoDoMinimo) {
        List<Peca> todas = pecas.listar();
        return somenteAbaixoDoMinimo ? todas.stream().filter(Peca::abaixoDoMinimo).toList() : todas;
    }

    public void remover(UUID id) {
        buscarPorId(id);
        pecas.remover(id);
    }

    public Peca reporEstoque(UUID id, int quantidade) {
        Peca peca = bloquear(id);
        peca.repor(quantidade);
        return pecas.salvar(peca);
    }

    public Peca baixarEstoque(UUID id, int quantidade) {
        Peca peca = bloquear(id);
        peca.baixar(quantidade, relogio.instant());
        Peca salva = pecas.salvar(peca);
        publicador.publicar(peca.extrairEventos());
        return salva;
    }

    private Peca bloquear(UUID id) {
        return pecas.buscarPorIdParaAtualizacao(id).orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
    }
}
