package br.com.oficina.application.ordemservico;

import java.util.UUID;

/**
 * Serviço ou peça solicitado, com a quantidade.
 */
public record ItemSolicitado(UUID id, int quantidade) {
}
