package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.veiculo.DadosVeiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VeiculoRequest(
        @Schema(description = "Padrão antigo (ABC1234) ou Mercosul (ABC1D23)", example = "BRA2E19") @NotBlank String placa,
        @Schema(example = "Volkswagen") @NotBlank @Size(max = 60) String marca,
        @Schema(example = "Gol") @NotBlank @Size(max = 80) String modelo,
        @Schema(example = "2020") @NotNull Integer ano,
        @NotNull UUID clienteId) {

    public DadosVeiculo paraDados() {
        return new DadosVeiculo(placa, marca, modelo, ano, clienteId);
    }
}
