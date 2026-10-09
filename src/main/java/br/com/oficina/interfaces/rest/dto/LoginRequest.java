package br.com.oficina.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@Schema(example = "admin") @NotBlank String usuario,
                           @NotBlank String senha) {
}
