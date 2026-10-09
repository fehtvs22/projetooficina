package br.com.oficina.domain.ordemservico;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro imutável de cada mudança de status da ordem de serviço (trilha de auditoria).
 */
public record HistoricoStatus(UUID id, StatusOrdemServico status, Instant ocorridoEm) {

    public static HistoricoStatus de(StatusOrdemServico status, Instant ocorridoEm) {
        return new HistoricoStatus(UUID.randomUUID(), status, ocorridoEm);
    }
}
