package br.com.oficina.domain.servico;

import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicoTest {

    @Test
    void cadastraEAtualizaServico() {
        Servico s = Servico.cadastrar("Alinhamento", "Direção", Dinheiro.de("90"), 45);

        s.atualizar("Alinhamento 3D", null, Dinheiro.de("110"), 50);

        assertThat(s.getNome()).isEqualTo("Alinhamento 3D");
        assertThat(s.getDescricao()).isNull();
        assertThat(s.getPreco()).isEqualTo(Dinheiro.de("110"));
        assertThat(s.getTempoEstimadoMinutos()).isEqualTo(50);
    }

    @Test
    void validaCampos() {
        assertThatThrownBy(() -> Servico.cadastrar("", null, Dinheiro.de("1"), 10)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Servico.cadastrar("X", null, null, 10)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Servico.cadastrar("X", null, Dinheiro.de("1"), 0)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void igualdadePorIdentificador() {
        UUID id = UUID.randomUUID();
        assertThat(Servico.reconstituir(id, "A", null, Dinheiro.ZERO, 1))
                .isEqualTo(Servico.reconstituir(id, "B", null, Dinheiro.ZERO, 2))
                .hasSameHashCodeAs(Servico.reconstituir(id, "C", null, Dinheiro.ZERO, 3));
    }
}
