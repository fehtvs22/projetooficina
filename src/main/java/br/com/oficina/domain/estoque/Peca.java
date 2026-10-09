package br.com.oficina.domain.estoque;

import br.com.oficina.domain.shared.AgregadoRaiz;
import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.Dinheiro;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Peça/Insumo com controle de estoque. Toda movimentação passa por este agregado,
 * garantindo que o saldo nunca fique negativo.
 */
public class Peca extends AgregadoRaiz {

    private final UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private TipoPeca tipo;
    private Dinheiro preco;
    private int quantidadeEstoque;
    private int estoqueMinimo;

    private Peca(UUID id, String codigo, String nome, String descricao, TipoPeca tipo, Dinheiro preco,
                 int quantidadeEstoque, int estoqueMinimo) {
        this.id = Assercoes.obrigatorio(id, "Id da peça");
        this.quantidadeEstoque = Assercoes.naoNegativo(quantidadeEstoque, "Quantidade em estoque");
        atualizar(codigo, nome, descricao, tipo, preco, estoqueMinimo);
    }

    public static Peca cadastrar(String codigo, String nome, String descricao, TipoPeca tipo, Dinheiro preco,
                                 int quantidadeInicial, int estoqueMinimo) {
        return new Peca(UUID.randomUUID(), codigo, nome, descricao, tipo, preco, quantidadeInicial, estoqueMinimo);
    }

    public static Peca reconstituir(UUID id, String codigo, String nome, String descricao, TipoPeca tipo,
                                    Dinheiro preco, int quantidadeEstoque, int estoqueMinimo) {
        return new Peca(id, codigo, nome, descricao, tipo, preco, quantidadeEstoque, estoqueMinimo);
    }

    public void atualizar(String codigo, String nome, String descricao, TipoPeca tipo, Dinheiro preco,
                          int estoqueMinimo) {
        this.codigo = Assercoes.textoObrigatorio(codigo, "Código da peça").toUpperCase();
        this.nome = Assercoes.textoObrigatorio(nome, "Nome da peça");
        this.descricao = Assercoes.textoOpcional(descricao);
        this.tipo = Assercoes.obrigatorio(tipo, "Tipo da peça");
        this.preco = Assercoes.obrigatorio(preco, "Preço da peça");
        this.estoqueMinimo = Assercoes.naoNegativo(estoqueMinimo, "Estoque mínimo");
    }

    public boolean possuiDisponivel(int quantidade) {
        return quantidadeEstoque >= quantidade;
    }

    /**
     * Entrada de estoque (compra/reposição).
     */
    public void repor(int quantidade) {
        Assercoes.positivo(quantidade, "Quantidade de reposição");
        quantidadeEstoque += quantidade;
    }

    /**
     * Saída de estoque (consumo em uma ordem de serviço ou ajuste).
     */
    public void baixar(int quantidade, Instant momento) {
        Assercoes.positivo(quantidade, "Quantidade de baixa");
        if (!possuiDisponivel(quantidade)) {
            throw new EstoqueInsuficienteException(nome, quantidadeEstoque, quantidade);
        }
        quantidadeEstoque -= quantidade;
        if (abaixoDoMinimo()) {
            registrarEvento(new EstoqueAbaixoDoMinimo(id, codigo, nome, quantidadeEstoque, estoqueMinimo, momento));
        }
    }

    public boolean abaixoDoMinimo() {
        return quantidadeEstoque < estoqueMinimo;
    }

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoPeca getTipo() {
        return tipo;
    }

    public Dinheiro getPreco() {
        return preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Peca outra && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
