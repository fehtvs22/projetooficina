package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * O status da ordem de serviço mudou (permite notificar o cliente do andamento).
 */
public record StatusOrdemServicoAlterado(UUID ordemServicoId, UUID clienteId, StatusOrdemServico anterior, StatusOrdemServico atual, Instant ocorridoEm) implements EventoDominio {
}
