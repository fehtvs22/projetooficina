package br.com.oficina.domain.cliente;

import br.com.oficina.domain.shared.Email;
import br.com.oficina.domain.shared.ValidacaoException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private static final Documento CPF = Documento.de("52998224725");

    @Test
    void cadastraClienteComIdentidadePropria() {
        Cliente cliente = Cliente.cadastrar(" Maria ", CPF, new Email("Maria@Email.com"), " 1199999 ");

        assertThat(cliente.getId()).isNotNull();
        assertThat(cliente.getNome()).isEqualTo("Maria");
        assertThat(cliente.getEmail().endereco()).isEqualTo("maria@email.com");
        assertThat(cliente.getTelefone()).isEqualTo("1199999");
        assertThat(cliente.possuiDocumento(Documento.de("529.982.247-25"))).isTrue();
    }

    @Test
    void exigeNome() {
        assertThatThrownBy(() -> Cliente.cadastrar(" ", CPF, null, null))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("Nome");
    }

    @Test
    void atualizaContatoMantendoDocumento() {
        Cliente cliente = Cliente.cadastrar("Maria", CPF, null, null);

        cliente.atualizarContato("Maria Souza", Email.deOpcional("m@x.com"), "");

        assertThat(cliente.getNome()).isEqualTo("Maria Souza");
        assertThat(cliente.getEmail()).isEqualTo(new Email("m@x.com"));
        assertThat(cliente.getTelefone()).isNull();
        assertThat(cliente.getDocumento()).isEqualTo(CPF);
    }

    @Test
    void igualdadePorIdentificador() {
        UUID id = UUID.randomUUID();
        Cliente a = Cliente.reconstituir(id, "A", CPF, null, null);
        Cliente b = Cliente.reconstituir(id, "B", CPF, null, null);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(Cliente.cadastrar("A", CPF, null, null));
    }
}
