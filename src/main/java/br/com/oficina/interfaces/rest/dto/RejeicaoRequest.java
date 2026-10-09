package br.com.oficina.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record RejeicaoRequest(@Schema(example = "Valor acima do esperado") @Size(max = 500) String motivo) {
}
