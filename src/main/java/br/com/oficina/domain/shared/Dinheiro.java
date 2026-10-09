package br.com.oficina.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Objeto de valor monetário (R$), sempre com duas casas decimais e nunca negativo.
 */
public record Dinheiro(BigDecimal valor) {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        if (valor == null) {
            throw new ValidacaoException("Valor monetário é obrigatório");
        }
        if (valor.signum() < 0) {
            throw new ValidacaoException("Valor monetário não pode ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro multiplicar(int quantidade) {
        return new Dinheiro(valor.multiply(BigDecimal.valueOf(quantidade)));
    }
}
