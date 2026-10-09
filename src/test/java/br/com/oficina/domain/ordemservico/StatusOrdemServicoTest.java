package br.com.oficina.domain.ordemservico;

import org.junit.jupiter.api.Test;

import static br.com.oficina.domain.ordemservico.StatusOrdemServico.AGUARDANDO_APROVACAO;
import static br.com.oficina.domain.ordemservico.StatusOrdemServico.EM_DIAGNOSTICO;
import static br.com.oficina.domain.ordemservico.StatusOrdemServico.EM_EXECUCAO;
import static br.com.oficina.domain.ordemservico.StatusOrdemServico.ENTREGUE;
import static br.com.oficina.domain.ordemservico.StatusOrdemServico.FINALIZADA;
import static br.com.oficina.domain.ordemservico.StatusOrdemServico.RECEBIDA;
import static org.assertj.core.api.Assertions.assertThat;

class StatusOrdemServicoTest {

    @Test
    void transicoesPermitidas() {
        assertThat(RECEBIDA.proximosPermitidos()).containsExactly(EM_DIAGNOSTICO);
        assertThat(EM_DIAGNOSTICO.proximosPermitidos()).containsExactly(AGUARDANDO_APROVACAO);
        assertThat(AGUARDANDO_APROVACAO.proximosPermitidos()).containsExactlyInAnyOrder(EM_EXECUCAO, FINALIZADA);
        assertThat(EM_EXECUCAO.proximosPermitidos()).containsExactly(FINALIZADA);
        assertThat(FINALIZADA.proximosPermitidos()).containsExactly(ENTREGUE);
        assertThat(ENTREGUE.proximosPermitidos()).isEmpty();
    }

    @Test
    void naoVoltaStatus() {
        assertThat(EM_EXECUCAO.podeTransicionarPara(EM_DIAGNOSTICO)).isFalse();
        assertThat(FINALIZADA.podeTransicionarPara(EM_EXECUCAO)).isFalse();
    }

    @Test
    void itensEditaveisSomenteAntesDoOrcamento() {
        assertThat(RECEBIDA.permiteAlterarItens()).isTrue();
        assertThat(EM_DIAGNOSTICO.permiteAlterarItens()).isTrue();
        assertThat(AGUARDANDO_APROVACAO.permiteAlterarItens()).isFalse();
        assertThat(EM_EXECUCAO.permiteAlterarItens()).isFalse();
    }
}
