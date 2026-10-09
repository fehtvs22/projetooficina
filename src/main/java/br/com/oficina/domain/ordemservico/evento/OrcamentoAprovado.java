package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * O cliente aprovou o orçamento; a execução do serviço pode começar.
 */
public record OrcamentoAprovado(UUID ordemServicoId, UUID clienteId, Dinheiro valorTotal, Instant ocorridoEm) implements EventoDominio {
}
