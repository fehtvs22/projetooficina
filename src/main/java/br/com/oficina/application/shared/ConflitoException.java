package br.com.oficina.application.shared;

/**
 * Operação conflita com o estado atual (ex.: documento ou placa já cadastrados).
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
