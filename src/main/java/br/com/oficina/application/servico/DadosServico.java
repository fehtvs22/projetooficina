package br.com.oficina.application.servico;

import java.math.BigDecimal;

public record DadosServico(String nome, String descricao, BigDecimal preco, int tempoEstimadoMinutos) {
}
