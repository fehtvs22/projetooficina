package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.Dinheiro;

import java.util.UUID;

/**
 * Entidade interna do agregado OrdemServico: peça ou insumo necessário, com preço congelado na inclusão.
 */
public class ItemPeca {

    private final UUID id;
    private final UUID pecaId;
    private final String descricao;
    private final Dinheiro precoUnitario;
    private int quantidade;

    public ItemPeca(UUID id, UUID pecaId, String descricao, Dinheiro precoUnitario, int quantidade) {
        this.id = Assercoes.obrigatorio(id, "Id do item");
        this.pecaId = Assercoes.obrigatorio(pecaId, "Peça");
        this.descricao = Assercoes.textoObrigatorio(descricao, "Descrição da peça");
        this.precoUnitario = Assercoes.obrigatorio(precoUnitario, "Preço da peça");
        this.quantidade = Assercoes.positivo(quantidade, "Quantidade");
    }

    void acrescentar(int quantidade) {
        this.quantidade += Assercoes.positivo(quantidade, "Quantidade");
    }

    public Dinheiro subtotal() {
        return precoUnitario.multiplicar(quantidade);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPecaId() {
        return pecaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public Dinheiro getPrecoUnitario() {
        return precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
