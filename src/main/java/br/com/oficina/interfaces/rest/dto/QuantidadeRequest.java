package br.com.oficina.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuantidadeRequest(@Schema(example = "5") @NotNull @Positive Integer quantidade) {
}
