package br.com.oficina.domain.shared;

/**
 * Guardas de invariantes reutilizadas pelas entidades e objetos de valor.
 */
public final class Assercoes {

    private Assercoes() {
    }

    public static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException(campo + " é obrigatório");
        }
        return valor.trim();
    }

    public static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new ValidacaoException(campo + " é obrigatório");
        }
        return valor;
    }

    public static int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new ValidacaoException(campo + " deve ser maior que zero");
        }
        return valor;
    }

    public static int naoNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new ValidacaoException(campo + " não pode ser negativo");
        }
        return valor;
    }
}
