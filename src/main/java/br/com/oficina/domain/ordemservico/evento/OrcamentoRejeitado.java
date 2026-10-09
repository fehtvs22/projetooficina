package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * O cliente rejeitou o orçamento; a ordem é finalizada sem execução.
 */
public record OrcamentoRejeitado(UUID ordemServicoId, UUID clienteId, String motivo, Instant ocorridoEm) implements EventoDominio {
}
