package br.com.oficina.domain.servico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepository {

    Servico salvar(Servico servico);

    Optional<Servico> buscarPorId(UUID id);

    List<Servico> listar();

    void remover(UUID id);
}
