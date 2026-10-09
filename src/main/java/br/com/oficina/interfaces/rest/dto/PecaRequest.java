package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.estoque.DadosPeca;
import br.com.oficina.domain.estoque.TipoPeca;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PecaRequest(
        @Schema(example = "FILTRO-AR") @NotBlank @Size(max = 40) String codigo,
        @Schema(example = "Filtro de ar") @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull TipoPeca tipo,
        @Schema(example = "55.00") @NotNull @DecimalMin("0.00") BigDecimal preco,
        @Schema(description = "Saldo inicial (ignorado na atualização; use as rotas de estoque)", example = "10")
        @PositiveOrZero Integer quantidadeInicial,
        @Schema(example = "2") @NotNull @PositiveOrZero Integer estoqueMinimo) {

    public DadosPeca paraDados() {
        return new DadosPeca(codigo, nome, descricao, tipo, preco, quantidadeInicial == null ? 0 : quantidadeInicial,
                estoqueMinimo);
    }
}
