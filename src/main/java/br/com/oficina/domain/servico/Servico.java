package br.com.oficina.domain.servico;

import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.Dinheiro;

import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Serviço: item do catálogo de mão de obra da oficina (ex.: troca de óleo, alinhamento).
 */
public class Servico {

    private final UUID id;
    private String nome;
    private String descricao;
    private Dinheiro preco;
    private int tempoEstimadoMinutos;

    private Servico(UUID id, String nome, String descricao, Dinheiro preco, int tempoEstimadoMinutos) {
        this.id = Assercoes.obrigatorio(id, "Id do serviço");
        atualizar(nome, descricao, preco, tempoEstimadoMinutos);
    }

    public static Servico cadastrar(String nome, String descricao, Dinheiro preco, int tempoEstimadoMinutos) {
        return new Servico(UUID.randomUUID(), nome, descricao, preco, tempoEstimadoMinutos);
    }

    public static Servico reconstituir(UUID id, String nome, String descricao, Dinheiro preco, int tempoEstimadoMinutos) {
        return new Servico(id, nome, descricao, preco, tempoEstimadoMinutos);
    }

    public void atualizar(String nome, String descricao, Dinheiro preco, int tempoEstimadoMinutos) {
        this.nome = Assercoes.textoObrigatorio(nome, "Nome do serviço");
        this.descricao = Assercoes.textoOpcional(descricao);
        this.preco = Assercoes.obrigatorio(preco, "Preço do serviço");
        this.tempoEstimadoMinutos = Assercoes.positivo(tempoEstimadoMinutos, "Tempo estimado");
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Dinheiro getPreco() {
        return preco;
    }

    public int getTempoEstimadoMinutos() {
        return tempoEstimadoMinutos;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Servico outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
