package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.domain.estoque.Peca;

import java.math.BigDecimal;
import java.util.UUID;

public record PecaResponse(UUID id, String codigo, String nome, String descricao, String tipo, BigDecimal preco,
                           int quantidadeEstoque, int estoqueMinimo, boolean abaixoDoMinimo) {

    public static PecaResponse de(Peca p) {
        return new PecaResponse(p.getId(), p.getCodigo(), p.getNome(), p.getDescricao(), p.getTipo().name(),
                p.getPreco().valor(), p.getQuantidadeEstoque(), p.getEstoqueMinimo(), p.abaixoDoMinimo());
    }
}
