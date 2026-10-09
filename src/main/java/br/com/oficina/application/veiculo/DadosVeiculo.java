package br.com.oficina.application.veiculo;

import java.util.UUID;

public record DadosVeiculo(String placa, String marca, String modelo, int ano, UUID clienteId) {
}
