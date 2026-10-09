package br.com.oficina.domain.veiculo;

import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeiculoTest {

    private final UUID clienteId = UUID.randomUUID();

    @Test
    void cadastraVeiculoDoCliente() {
        Veiculo v = Veiculo.cadastrar(Placa.de("BRA2E19"), "VW", "Gol", 2020, clienteId);

        assertThat(v.getId()).isNotNull();
        assertThat(v.pertenceA(clienteId)).isTrue();
        assertThat(v.pertenceA(UUID.randomUUID())).isFalse();
        assertThat(v.getMarca()).isEqualTo("VW");
        assertThat(v.getModelo()).isEqualTo("Gol");
        assertThat(v.getAno()).isEqualTo(2020);
    }

    @Test
    void validaFaixaDoAno() {
        int proximoAno = Year.now().getValue() + 1;
        assertThat(Veiculo.cadastrar(Placa.de("ABC1234"), "VW", "Gol", proximoAno, clienteId).getAno())
                .isEqualTo(proximoAno);
        assertThatThrownBy(() -> Veiculo.cadastrar(Placa.de("ABC1234"), "VW", "Gol", 1899, clienteId))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(Placa.de("ABC1234"), "VW", "Gol", proximoAno + 1, clienteId))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void exigeMarcaModeloECliente() {
        assertThatThrownBy(() -> Veiculo.cadastrar(Placa.de("ABC1234"), "", "Gol", 2020, clienteId))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(Placa.de("ABC1234"), "VW", null, 2020, clienteId))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(Placa.de("ABC1234"), "VW", "Gol", 2020, null))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void atualizaDadosETrocaProprietario() {
        Veiculo v = Veiculo.cadastrar(Placa.de("ABC1234"), "VW", "Gol", 2020, clienteId);
        UUID novoDono = UUID.randomUUID();

        v.atualizar("Fiat", "Uno", 2015, novoDono);

        assertThat(v.getMarca()).isEqualTo("Fiat");
        assertThat(v.getClienteId()).isEqualTo(novoDono);
        assertThat(v.getPlaca().valor()).isEqualTo("ABC1234");
    }

    @Test
    void igualdadePorIdentificador() {
        UUID id = UUID.randomUUID();
        Veiculo a = Veiculo.reconstituir(id, Placa.de("ABC1234"), "VW", "Gol", 2020, clienteId);
        Veiculo b = Veiculo.reconstituir(id, Placa.de("ABC1234"), "Fiat", "Uno", 2010, clienteId);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
