package br.com.oficina.domain.shared;

/**
 * Dado de entrada inválido segundo as invariantes do domínio (ex.: CPF inválido, placa inválida).
 */
public class ValidacaoException extends DomainException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
