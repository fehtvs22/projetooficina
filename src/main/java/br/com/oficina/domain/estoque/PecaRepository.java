package br.com.oficina.domain.estoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PecaRepository {

    Peca salvar(Peca peca);

    Optional<Peca> buscarPorId(UUID id);

    /**
     * Busca a peça bloqueando-a para atualização (evita baixa concorrente do mesmo saldo).
     */
    Optional<Peca> buscarPorIdParaAtualizacao(UUID id);

    Optional<Peca> buscarPorCodigo(String codigo);

    List<Peca> listar();

    void remover(UUID id);
}
