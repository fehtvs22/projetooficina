package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.servico.DadosServico;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequest(
        @Schema(example = "Troca de óleo") @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @Schema(example = "120.00") @NotNull @DecimalMin("0.00") BigDecimal preco,
        @Schema(example = "40") @NotNull @Positive Integer tempoEstimadoMinutos) {

    public DadosServico paraDados() {
        return new DadosServico(nome, descricao, preco, tempoEstimadoMinutos);
    }
}
