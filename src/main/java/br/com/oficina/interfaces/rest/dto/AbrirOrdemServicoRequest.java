package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.ordemservico.AbrirOrdemServicoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AbrirOrdemServicoRequest(
        @Schema(description = "CPF/CNPJ do cliente já cadastrado", example = "529.982.247-25") @NotBlank String documentoCliente,
        @NotNull @Valid VeiculoOrdem veiculo,
        @Schema(example = "Barulho ao frear") @Size(max = 1000) String descricaoProblema,
        List<@Valid ItemRequest> servicos,
        List<@Valid ItemRequest> pecas) {

    @Schema(description = "Se a placa já existir para o cliente, o veículo é reutilizado; senão é cadastrado")
    public record VeiculoOrdem(@Schema(example = "BRA2E19") @NotBlank String placa,
                               @Schema(example = "Volkswagen") @Size(max = 60) String marca,
                               @Schema(example = "Gol") @Size(max = 80) String modelo,
                               @Schema(example = "2020") Integer ano) {
    }

    public AbrirOrdemServicoCommand paraComando() {
        return new AbrirOrdemServicoCommand(documentoCliente,
                new AbrirOrdemServicoCommand.Veiculo(veiculo.placa(), veiculo.marca(), veiculo.modelo(), veiculo.ano()),
                descricaoProblema,
                servicos == null ? List.of() : servicos.stream().map(ItemRequest::paraItem).toList(),
                pecas == null ? List.of() : pecas.stream().map(ItemRequest::paraItem).toList());
    }
}
