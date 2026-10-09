package br.com.oficina.domain.estoque;

import br.com.oficina.domain.shared.RegraNegocioException;

public class EstoqueInsuficienteException extends RegraNegocioException {

    public EstoqueInsuficienteException(String nomePeca, int disponivel, int solicitado) {
        super("Estoque insuficiente para '" + nomePeca + "': disponível " + disponivel + ", solicitado " + solicitado);
    }
}
