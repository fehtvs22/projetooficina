package br.com.oficina.application.autenticacao;

import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutenticarUsuarioUseCaseTest {

    private final AutenticacaoPort porta = (usuario, senha) -> {
        if (usuario.equals("admin") && senha.equals("segredo")) {
            return new TokenAcesso("jwt", "Bearer", 3600);
        }
        throw new CredenciaisInvalidasException();
    };
    private final AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(porta);

    @Test
    void autenticaComCredenciaisValidas() {
        assertThat(useCase.autenticar("admin", "segredo").token()).isEqualTo("jwt");
    }

    @Test
    void rejeitaCredenciaisInvalidasOuVazias() {
        assertThatThrownBy(() -> useCase.autenticar("admin", "errada")).isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> useCase.autenticar("", "x")).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> useCase.autenticar("admin", null)).isInstanceOf(ValidacaoException.class);
    }
}
