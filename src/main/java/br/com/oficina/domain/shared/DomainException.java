package br.com.oficina.domain.shared;

/**
 * Exceção base para violações de regras do domínio.
 */
public class DomainException extends RuntimeException {

    public DomainException(String mensagem) {
        super(mensagem);
    }
}
