package br.com.oficina.domain.ordemservico.evento;

import br.com.oficina.domain.shared.EventoDominio;

import java.time.Instant;
import java.util.UUID;

/**
 * O veículo foi entregue ao cliente, encerrando a ordem de serviço.
 */
public record VeiculoEntregue(UUID ordemServicoId, UUID clienteId, UUID veiculoId, Instant ocorridoEm) implements EventoDominio {
}
