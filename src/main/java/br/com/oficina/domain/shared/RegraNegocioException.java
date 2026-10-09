package br.com.oficina.domain.shared;

/**
 * Operação válida sintaticamente, mas que viola uma regra de negócio.
 */
public class RegraNegocioException extends DomainException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
