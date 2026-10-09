package br.com.oficina.domain.estoque;

import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

public record EstoqueAbaixoDoMinimo(UUID pecaId, String codigo, String nome, int quantidadeAtual,
                                    int estoqueMinimo, Instant ocorridoEm) implements EventoDominio {
}
