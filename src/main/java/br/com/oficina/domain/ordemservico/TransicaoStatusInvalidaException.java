package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.shared.RegraNegocioException;

public class TransicaoStatusInvalidaException extends RegraNegocioException {

    public TransicaoStatusInvalidaException(StatusOrdemServico atual, StatusOrdemServico destino) {
        super("Transição de status inválida: " + atual + " -> " + destino
                + ". Permitidos a partir de " + atual + ": " + atual.proximosPermitidos());
    }
}
