package br.com.oficina.domain.veiculo;

import br.com.oficina.domain.shared.ValidacaoException;

import java.util.regex.Pattern;

/**
 * Objeto de valor da placa do veículo. Aceita o padrão antigo (ABC1234) e o Mercosul (ABC1D23).
 */
public record Placa(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    public Placa {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Placa é obrigatória");
        }
        valor = valor.replaceAll("[\\s-]", "").toUpperCase();
        if (!FORMATO.matcher(valor).matches()) {
            throw new ValidacaoException("Placa inválida: use o padrão ABC1234 ou Mercosul ABC1D23");
        }
    }

    public static Placa de(String valor) {
        return new Placa(valor);
    }

    public boolean mercosul() {
        return Character.isLetter(valor.charAt(4));
    }
}
