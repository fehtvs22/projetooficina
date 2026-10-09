package br.com.oficina.application.veiculo;

import br.com.oficina.application.fakes.EmMemoria;
import br.com.oficina.application.shared.ConflitoException;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GerenciarVeiculosUseCaseTest {

    private final EmMemoria.Veiculos veiculos = new EmMemoria.Veiculos();
    private final EmMemoria.Clientes clientes = new EmMemoria.Clientes();
    private final EmMemoria.Ordens ordens = new EmMemoria.Ordens();
    private final GerenciarVeiculosUseCase useCase = new GerenciarVeiculosUseCase(veiculos, clientes, ordens);
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = clientes.salvar(Cliente.cadastrar("Maria", Documento.de("52998224725"), null, null));
    }

    private Veiculo gol() {
        return useCase.cadastrar(new DadosVeiculo("bra-2e19", "VW", "Gol", 2020, cliente.getId()));
    }

    @Test
    void cadastraELista() {
        Veiculo gol = gol();

        assertThat(gol.getPlaca().valor()).isEqualTo("BRA2E19");
        assertThat(useCase.buscarPorId(gol.getId())).isEqualTo(gol);
        assertThat(useCase.listar(null)).containsExactly(gol);
        assertThat(useCase.listar(cliente.getId())).containsExactly(gol);
        assertThat(useCase.listar(UUID.randomUUID())).isEmpty();
    }

    @Test
    void exigeClienteExistenteEPlacaUnica() {
        gol();
        assertThatThrownBy(() -> useCase.cadastrar(new DadosVeiculo("BRA2E19", "VW", "Gol", 2020, cliente.getId())))
                .isInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> useCase.cadastrar(new DadosVeiculo("ABC1234", "VW", "Gol", 2020, UUID.randomUUID())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> useCase.cadastrar(new DadosVeiculo("ABC1234", "VW", "Gol", 2020, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizaSemTrocarPlaca() {
        Veiculo gol = gol();

        Veiculo atualizado = useCase.atualizar(gol.getId(), new DadosVeiculo(null, "VW", "Gol G5", 2021, cliente.getId()));
        assertThat(atualizado.getModelo()).isEqualTo("Gol G5");
        assertThat(useCase.atualizar(gol.getId(), new DadosVeiculo("BRA2E19", "VW", "Gol", 2021, cliente.getId())))
                .isNotNull();

        assertThatThrownBy(() -> useCase.atualizar(gol.getId(),
                new DadosVeiculo("XYZ9876", "VW", "Gol", 2021, cliente.getId())))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void removeSomenteSemOrdemDeServico() {
        Veiculo gol = gol();
        Veiculo outro = useCase.cadastrar(new DadosVeiculo("ABC1234", "Fiat", "Uno", 2010, cliente.getId()));
        ordens.salvar(OrdemServico.abrir(cliente.getId(), gol.getId(), null, Instant.now()));

        assertThatThrownBy(() -> useCase.remover(gol.getId())).isInstanceOf(RegraNegocioException.class);
        useCase.remover(outro.getId());
        assertThat(useCase.listar(null)).containsExactly(gol);
    }
}
