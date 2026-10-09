package br.com.oficina.application.estoque;

import br.com.oficina.application.fakes.EmMemoria;
import br.com.oficina.application.shared.ConflitoException;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.estoque.EstoqueAbaixoDoMinimo;
import br.com.oficina.domain.estoque.EstoqueInsuficienteException;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.TipoPeca;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GerenciarPecasUseCaseTest {

    private final EmMemoria.Pecas pecas = new EmMemoria.Pecas();
    private final EmMemoria.Eventos eventos = new EmMemoria.Eventos();
    private final GerenciarPecasUseCase useCase = new GerenciarPecasUseCase(pecas, eventos,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    private Peca filtro() {
        return useCase.cadastrar(new DadosPeca(" filtro ", "Filtro", null, TipoPeca.PECA, new BigDecimal("38.5"), 5, 2));
    }

    @Test
    void cadastraComCodigoUnico() {
        Peca filtro = filtro();

        assertThat(filtro.getCodigo()).isEqualTo("FILTRO");
        assertThat(useCase.buscarPorId(filtro.getId())).isEqualTo(filtro);
        assertThatThrownBy(this::filtro).isInstanceOf(ConflitoException.class);
    }

    @Test
    void atualizaValidandoCodigoDeOutraPeca() {
        Peca filtro = filtro();
        Peca oleo = useCase.cadastrar(new DadosPeca("OLEO", "Óleo", null, TipoPeca.INSUMO, BigDecimal.TEN, 10, 1));

        Peca atualizado = useCase.atualizar(filtro.getId(),
                new DadosPeca("FILTRO", "Filtro de óleo", "x", TipoPeca.PECA, BigDecimal.ONE, 999, 3));
        assertThat(atualizado.getNome()).isEqualTo("Filtro de óleo");
        assertThat(atualizado.getQuantidadeEstoque()).as("saldo não muda pela atualização").isEqualTo(5);

        assertThatThrownBy(() -> useCase.atualizar(oleo.getId(),
                new DadosPeca("filtro", "Óleo", null, TipoPeca.INSUMO, BigDecimal.TEN, 0, 1)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void movimentaEstoqueComBloqueioEPublicaAlerta() {
        Peca filtro = filtro();

        assertThat(useCase.reporEstoque(filtro.getId(), 5).getQuantidadeEstoque()).isEqualTo(10);
        assertThat(useCase.baixarEstoque(filtro.getId(), 9).getQuantidadeEstoque()).isEqualTo(1);

        assertThat(pecas.bloqueios).isEqualTo(2);
        assertThat(eventos.doTipo(EstoqueAbaixoDoMinimo.class)).hasSize(1);
        assertThat(useCase.listar(true)).containsExactly(filtro);
        assertThat(useCase.listar(false)).containsExactly(filtro);
        assertThatThrownBy(() -> useCase.baixarEstoque(filtro.getId(), 2)).isInstanceOf(EstoqueInsuficienteException.class);
    }

    @Test
    void removeEPecaInexistente() {
        Peca filtro = filtro();
        useCase.remover(filtro.getId());

        assertThat(useCase.listar(false)).isEmpty();
        assertThatThrownBy(() -> useCase.buscarPorId(filtro.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> useCase.reporEstoque(UUID.randomUUID(), 1))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
