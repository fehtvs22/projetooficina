package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.domain.veiculo.Veiculo;

import java.util.UUID;

public record VeiculoResponse(UUID id, String placa, String marca, String modelo, int ano, UUID clienteId) {

    public static VeiculoResponse de(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getPlaca().valor(), v.getMarca(), v.getModelo(), v.getAno(),
                v.getClienteId());
    }
}
