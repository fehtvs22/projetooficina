package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.cliente.DadosCliente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @Schema(example = "Maria da Silva") @NotBlank @Size(max = 150) String nome,
        @Schema(description = "CPF ou CNPJ, com ou sem máscara", example = "529.982.247-25") @NotBlank String documento,
        @Schema(example = "maria@email.com") @Email @Size(max = 150) String email,
        @Schema(example = "(11) 98888-7777") @Size(max = 20) String telefone) {

    public DadosCliente paraDados() {
        return new DadosCliente(nome, documento, email, telefone);
    }
}
