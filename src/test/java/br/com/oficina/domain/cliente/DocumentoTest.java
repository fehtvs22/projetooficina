package br.com.oficina.domain.cliente;

import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", " 529 982 247 25 "})
    void aceitaCpfValidoComOuSemMascara(String valor) {
        Documento doc = Documento.de(valor);

        assertThat(doc.tipo()).isEqualTo(TipoDocumento.CPF);
        assertThat(doc.numero()).isEqualTo("52998224725");
        assertThat(doc.formatado()).isEqualTo("529.982.247-25");
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-24", "111.111.111-11", "000.000.000-00", "123.456.789-00"})
    void rejeitaCpfComDigitoVerificadorInvalido(String valor) {
        assertThatThrownBy(() -> Documento.de(valor))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("CPF inválido");
    }

    @Test
    void aceitaCnpjNumericoValido() {
        Documento doc = Documento.de("11.222.333/0001-81");

        assertThat(doc.tipo()).isEqualTo(TipoDocumento.CNPJ);
        assertThat(doc.numero()).isEqualTo("11222333000181");
        assertThat(doc.formatado()).isEqualTo("11.222.333/0001-81");
    }

    @Test
    void aceitaCnpjAlfanumerico() {
        Documento doc = Documento.de("12.abc.345/01de-35");

        assertThat(doc.tipo()).isEqualTo(TipoDocumento.CNPJ);
        assertThat(doc.numero()).isEqualTo("12ABC34501DE35");
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-82", "11.111.111/1111-11", "12.ABC.345/01DE-36", "12.ABC.345/01DE-3A"})
    void rejeitaCnpjInvalido(String valor) {
        assertThatThrownBy(() -> Documento.de(valor))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("CNPJ inválido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "1234567890123456", "ABCDEFGHIJK"})
    void rejeitaTamanhoInvalido(String valor) {
        assertThatThrownBy(() -> Documento.de(valor))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("CPF (11 dígitos) ou CNPJ");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejeitaDocumentoAusente(String valor) {
        assertThatThrownBy(() -> Documento.de(valor)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void construtorExigeNumeroETipo() {
        assertThatThrownBy(() -> new Documento(null, TipoDocumento.CPF)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> new Documento("52998224725", null)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void igualdadePorValor() {
        assertThat(Documento.de("529.982.247-25")).isEqualTo(Documento.de("52998224725"));
    }
}
