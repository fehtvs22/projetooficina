package br.com.oficina.domain.ordemservico;

import java.util.Set;

/**
 * Ciclo de vida da Ordem de Serviço e as transições permitidas entre os status.
 */
public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE;

    public Set<StatusOrdemServico> proximosPermitidos() {
        return switch (this) {
            case RECEBIDA -> Set.of(EM_DIAGNOSTICO);
            case EM_DIAGNOSTICO -> Set.of(AGUARDANDO_APROVACAO);
            // Aprovado -> execução; rejeitado -> finalizada sem execução (veículo aguarda retirada)
            case AGUARDANDO_APROVACAO -> Set.of(EM_EXECUCAO, FINALIZADA);
            case EM_EXECUCAO -> Set.of(FINALIZADA);
            case FINALIZADA -> Set.of(ENTREGUE);
            case ENTREGUE -> Set.of();
        };
    }

    public boolean podeTransicionarPara(StatusOrdemServico destino) {
        return proximosPermitidos().contains(destino);
    }

    public boolean permiteAlterarItens() {
        return this == RECEBIDA || this == EM_DIAGNOSTICO;
    }
}
