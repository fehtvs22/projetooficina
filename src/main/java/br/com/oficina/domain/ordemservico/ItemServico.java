package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.Dinheiro;

import java.util.UUID;

/**
 * Entidade interna do agregado OrdemServico: serviço incluído com o preço vigente no momento da inclusão.
 */
public class ItemServico {

    private final UUID id;
    private final UUID servicoId;
    private final String descricao;
    private final Dinheiro precoUnitario;
    private int quantidade;

    public ItemServico(UUID id, UUID servicoId, String descricao, Dinheiro precoUnitario, int quantidade) {
        this.id = Assercoes.obrigatorio(id, "Id do item");
        this.servicoId = Assercoes.obrigatorio(servicoId, "Serviço");
        this.descricao = Assercoes.textoObrigatorio(descricao, "Descrição do serviço");
        this.precoUnitario = Assercoes.obrigatorio(precoUnitario, "Preço do serviço");
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

    public UUID getServicoId() {
        return servicoId;
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
