package br.com.oficina.domain.estoque;

import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PecaTest {

    private static final Instant AGORA = Instant.parse("2026-01-10T10:00:00Z");

    private Peca filtro(int estoque, int minimo) {
        return Peca.cadastrar("filtro-1", "Filtro de óleo", null, TipoPeca.PECA, Dinheiro.de("38.50"), estoque, minimo);
    }

    @Test
    void cadastraComCodigoNormalizado() {
        Peca p = filtro(10, 2);

        assertThat(p.getCodigo()).isEqualTo("FILTRO-1");
        assertThat(p.getTipo()).isEqualTo(TipoPeca.PECA);
        assertThat(p.getQuantidadeEstoque()).isEqualTo(10);
        assertThat(p.abaixoDoMinimo()).isFalse();
    }

    @Test
    void reporAumentaSaldo() {
        Peca p = filtro(1, 0);

        p.repor(4);

        assertThat(p.getQuantidadeEstoque()).isEqualTo(5);
        assertThatThrownBy(() -> p.repor(0)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void baixarReduzSaldoSemEventoQuandoAcimaDoMinimo() {
        Peca p = filtro(10, 2);

        p.baixar(3, AGORA);

        assertThat(p.getQuantidadeEstoque()).isEqualTo(7);
        assertThat(p.extrairEventos()).isEmpty();
    }

    @Test
    void baixarAbaixoDoMinimoGeraEvento() {
        Peca p = filtro(3, 2);

        p.baixar(2, AGORA);

        assertThat(p.abaixoDoMinimo()).isTrue();
        assertThat(p.extrairEventos()).singleElement()
                .isInstanceOfSatisfying(EstoqueAbaixoDoMinimo.class, e -> {
                    assertThat(e.quantidadeAtual()).isEqualTo(1);
                    assertThat(e.estoqueMinimo()).isEqualTo(2);
                    assertThat(e.ocorridoEm()).isEqualTo(AGORA);
                });
        assertThat(p.extrairEventos()).as("eventos são extraídos uma única vez").isEmpty();
    }

    @Test
    void naoPermiteSaldoNegativo() {
        Peca p = filtro(2, 0);

        assertThatThrownBy(() -> p.baixar(3, AGORA))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("disponível 2, solicitado 3");
        assertThat(p.getQuantidadeEstoque()).isEqualTo(2);
        assertThat(p.possuiDisponivel(2)).isTrue();
        assertThat(p.possuiDisponivel(3)).isFalse();
    }

    @Test
    void validaCadastro() {
        assertThatThrownBy(() -> Peca.cadastrar("X", "Y", null, TipoPeca.PECA, Dinheiro.ZERO, -1, 0))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Peca.cadastrar("X", "Y", null, null, Dinheiro.ZERO, 0, 0))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Peca.cadastrar("X", "Y", null, TipoPeca.INSUMO, Dinheiro.ZERO, 0, -1))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void atualizaCadastroSemAlterarSaldo() {
        Peca p = filtro(5, 1);

        p.atualizar("oleo", "Óleo", "5W30", TipoPeca.INSUMO, Dinheiro.de("40"), 3);

        assertThat(p.getCodigo()).isEqualTo("OLEO");
        assertThat(p.getDescricao()).isEqualTo("5W30");
        assertThat(p.getPreco()).isEqualTo(Dinheiro.de("40"));
        assertThat(p.getEstoqueMinimo()).isEqualTo(3);
        assertThat(p.getQuantidadeEstoque()).isEqualTo(5);
    }

    @Test
    void igualdadePorIdentificador() {
        UUID id = UUID.randomUUID();
        Peca a = Peca.reconstituir(id, "A", "A", null, TipoPeca.PECA, Dinheiro.ZERO, 1, 0);
        Peca b = Peca.reconstituir(id, "B", "B", null, TipoPeca.PECA, Dinheiro.ZERO, 2, 0);
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
