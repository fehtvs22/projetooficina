package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * A execução dos serviços foi concluída; o veículo está pronto para retirada.
 */
public record OrdemServicoFinalizada(UUID ordemServicoId, UUID clienteId, Instant ocorridoEm) implements EventoDominio {
}
