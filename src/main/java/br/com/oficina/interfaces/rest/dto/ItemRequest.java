package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.ordemservico.ItemSolicitado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemRequest(@Schema(description = "Id do serviço ou da peça/insumo") @NotNull UUID id,
                          @Schema(example = "1") @NotNull @Positive Integer quantidade) {

    public ItemSolicitado paraItem() {
        return new ItemSolicitado(id, quantidade);
    }
}
