package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * Uma nova ordem de serviço foi aberta para o veículo do cliente.
 */
public record OrdemServicoAberta(UUID ordemServicoId, UUID clienteId, UUID veiculoId, Instant ocorridoEm) implements EventoDominio {
}
