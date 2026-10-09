package br.com.oficina.application.estoque;

import br.com.oficina.domain.estoque.TipoPeca;

import java.math.BigDecimal;

public record DadosPeca(String codigo, String nome, String descricao, TipoPeca tipo, BigDecimal preco,
                        int quantidadeInicial, int estoqueMinimo) {
}
