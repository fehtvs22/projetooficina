package br.com.oficina.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RespostaOrcamentoRequest(
        @Schema(description = "true para aprovar, false para rejeitar", example = "true") @NotNull Boolean aprovado,
        @Schema(description = "Motivo (opcional) em caso de rejeição") @Size(max = 500) String motivo) {
}
