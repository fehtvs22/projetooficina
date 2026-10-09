package br.com.oficina.domain.veiculo;

import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlacaTest {

    @Test
    void aceitaPadraoAntigoComHifen() {
        Placa placa = Placa.de("abc-1234");

        assertThat(placa.valor()).isEqualTo("ABC1234");
        assertThat(placa.mercosul()).isFalse();
    }

    @Test
    void aceitaPadraoMercosul() {
        Placa placa = Placa.de("BRA2E19");

        assertThat(placa.valor()).isEqualTo("BRA2E19");
        assertThat(placa.mercosul()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB12345", "ABCD123", "ABC12345", "1BC1234", "ABC1D2E", "ABC-12"})
    void rejeitaFormatoInvalido(String valor) {
        assertThatThrownBy(() -> Placa.de(valor))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("Placa inválida");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejeitaPlacaAusente(String valor) {
        assertThatThrownBy(() -> Placa.de(valor)).isInstanceOf(ValidacaoException.class);
    }
}
