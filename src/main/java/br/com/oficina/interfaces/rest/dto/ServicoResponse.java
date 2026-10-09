package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.domain.servico.Servico;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoResponse(UUID id, String nome, String descricao, BigDecimal preco, int tempoEstimadoMinutos) {

    public static ServicoResponse de(Servico s) {
        return new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getPreco().valor(),
                s.getTempoEstimadoMinutos());
    }
}
