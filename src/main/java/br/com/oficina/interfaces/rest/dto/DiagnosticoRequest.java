package br.com.oficina.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiagnosticoRequest(
        @Schema(example = "Pastilhas dianteiras gastas; fluido de freio vencido") @NotBlank @Size(max = 2000)
        String diagnostico) {
}
