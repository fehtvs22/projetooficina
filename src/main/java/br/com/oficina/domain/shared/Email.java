package br.com.oficina.domain.shared;

import java.util.regex.Pattern;

/**
 * Objeto de valor de e-mail.
 */
public record Email(String endereco) {

    private static final Pattern FORMATO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public Email {
        if (endereco == null || !FORMATO.matcher(endereco.trim()).matches()) {
            throw new ValidacaoException("E-mail inválido: " + endereco);
        }
        endereco = endereco.trim().toLowerCase();
    }

    public static Email deOpcional(String endereco) {
        return endereco == null || endereco.isBlank() ? null : new Email(endereco);
    }
}
