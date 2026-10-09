package br.com.oficina.application.servico;

import br.com.oficina.application.fakes.EmMemoria;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.servico.Servico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GerenciarServicosUseCaseTest {

    private final GerenciarServicosUseCase useCase = new GerenciarServicosUseCase(new EmMemoria.Servicos());

    @Test
    void crudCompleto() {
        Servico s = useCase.cadastrar(new DadosServico("Alinhamento", null, new BigDecimal("90"), 45));
        assertThat(useCase.listar()).containsExactly(s);

        Servico atualizado = useCase.atualizar(s.getId(), new DadosServico("Alinhamento 3D", "x", new BigDecimal("100"), 50));
        assertThat(atualizado.getPreco().valor()).isEqualByComparingTo("100");
        assertThat(useCase.buscarPorId(s.getId()).getNome()).isEqualTo("Alinhamento 3D");

        useCase.remover(s.getId());
        assertThat(useCase.listar()).isEmpty();
    }

    @Test
    void servicoInexistente() {
        assertThatThrownBy(() -> useCase.remover(UUID.randomUUID())).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
