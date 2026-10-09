package br.com.oficina.application.cliente;

import br.com.oficina.application.fakes.EmMemoria;
import br.com.oficina.application.shared.ConflitoException;
import br.com.oficina.application.shared.RecursoNaoEncontradoException;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.shared.ValidacaoException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GerenciarClientesUseCaseTest {

    private final EmMemoria.Clientes clientes = new EmMemoria.Clientes();
    private final EmMemoria.Veiculos veiculos = new EmMemoria.Veiculos();
    private final GerenciarClientesUseCase useCase = new GerenciarClientesUseCase(clientes, veiculos);

    private Cliente maria() {
        return useCase.cadastrar(new DadosCliente("Maria", "529.982.247-25", "maria@email.com", "11999990000"));
    }

    @Test
    void cadastraEBuscaPorIdEDocumento() {
        Cliente maria = maria();

        assertThat(useCase.buscarPorId(maria.getId())).isEqualTo(maria);
        assertThat(useCase.buscarPorDocumento("52998224725")).isEqualTo(maria);
        assertThat(useCase.listar()).containsExactly(maria);
    }

    @Test
    void naoPermiteDocumentoDuplicado() {
        maria();
        assertThatThrownBy(() -> useCase.cadastrar(new DadosCliente("Outra", "52998224725", null, null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("529.982.247-25");
    }

    @Test
    void validaDocumentoEEmail() {
        assertThatThrownBy(() -> useCase.cadastrar(new DadosCliente("X", "123", null, null)))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> useCase.cadastrar(new DadosCliente("X", "52998224725", "invalido", null)))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void atualizaContatoMasNaoDocumento() {
        Cliente maria = maria();

        Cliente atualizada = useCase.atualizar(maria.getId(), new DadosCliente("Maria S.", null, null, "1133334444"));
        assertThat(atualizada.getNome()).isEqualTo("Maria S.");
        assertThat(atualizada.getEmail()).isNull();

        assertThat(useCase.atualizar(maria.getId(), new DadosCliente("Maria", "529.982.247-25", null, null)).getNome())
                .isEqualTo("Maria");
        assertThatThrownBy(() -> useCase.atualizar(maria.getId(), new DadosCliente("M", "11.222.333/0001-81", null, null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void removeClienteSemVeiculos() {
        Cliente maria = maria();
        useCase.remover(maria.getId());
        assertThat(useCase.listar()).isEmpty();
    }

    @Test
    void naoRemoveClienteComVeiculos() {
        Cliente maria = maria();
        veiculos.salvar(Veiculo.cadastrar(Placa.de("ABC1234"), "VW", "Gol", 2020, maria.getId()));

        assertThatThrownBy(() -> useCase.remover(maria.getId())).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void clienteInexistente() {
        assertThatThrownBy(() -> useCase.buscarPorId(UUID.randomUUID())).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> useCase.buscarPorDocumento("52998224725"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> useCase.remover(UUID.randomUUID())).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
