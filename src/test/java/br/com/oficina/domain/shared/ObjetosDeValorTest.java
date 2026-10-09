package br.com.oficina.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjetosDeValorTest {

    @Test
    void dinheiroSempreComDuasCasas() {
        assertThat(Dinheiro.de("10").valor()).isEqualByComparingTo("10.00");
        assertThat(Dinheiro.de("10.005").valor().scale()).isEqualTo(2);
        assertThat(Dinheiro.de("10")).isEqualTo(Dinheiro.de(new BigDecimal("10.00")));
    }

    @Test
    void dinheiroSomaEMultiplica() {
        Dinheiro total = Dinheiro.de("45.90").multiplicar(4).somar(Dinheiro.de("38.50"));

        assertThat(total.valor()).isEqualByComparingTo("222.10");
        assertThat(Dinheiro.ZERO.somar(Dinheiro.de("1")).valor()).isEqualByComparingTo("1.00");
    }

    @Test
    void dinheiroNaoAceitaNuloOuNegativo() {
        assertThatThrownBy(() -> Dinheiro.de((BigDecimal) null)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Dinheiro.de("-0.01")).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void emailValidoENormalizado() {
        assertThat(new Email(" Ana.Silva+os@Oficina.com.br ").endereco()).isEqualTo("ana.silva+os@oficina.com.br");
        assertThat(Email.deOpcional(null)).isNull();
        assertThat(Email.deOpcional(" ")).isNull();
    }

    @Test
    void emailInvalido() {
        assertThatThrownBy(() -> new Email("sem-arroba")).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> new Email("a@b")).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void assercoes() {
        assertThat(Assercoes.textoOpcional("  x ")).isEqualTo("x");
        assertThat(Assercoes.naoNegativo(0, "n")).isZero();
        assertThatThrownBy(() -> Assercoes.positivo(0, "Quantidade"))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Quantidade deve ser maior que zero");
        assertThatThrownBy(() -> Assercoes.naoNegativo(-1, "Estoque"))
                .hasMessage("Estoque não pode ser negativo");
        assertThatThrownBy(() -> Assercoes.obrigatorio(null, "Campo")).hasMessage("Campo é obrigatório");
    }
}
