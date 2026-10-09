package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * O orçamento foi gerado e disponibilizado ao cliente para aprovação.
 */
public record OrcamentoEnviado(UUID ordemServicoId, UUID clienteId, Dinheiro valorTotal, Instant ocorridoEm) implements EventoDominio {
}
