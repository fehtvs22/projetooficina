package br.com.oficina.domain.cliente;

import br.com.oficina.domain.shared.ValidacaoException;

/**
 * Objeto de valor que identifica o cliente: CPF (pessoa física) ou CNPJ (pessoa jurídica).
 * <p>
 * Valida os dígitos verificadores. O CNPJ aceita o formato alfanumérico definido pela
 * Receita Federal (12 primeiros caracteres [0-9A-Z] + 2 dígitos verificadores numéricos).
 */
public record Documento(String numero, TipoDocumento tipo) {

    private static final int[] PESOS_CNPJ_DV1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_DV2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Documento {
        if (numero == null || tipo == null) {
            throw new ValidacaoException("Documento é obrigatório");
        }
    }

    /**
     * Cria o documento a partir de um texto com ou sem máscara, detectando se é CPF ou CNPJ.
     */
    public static Documento de(String valor) {
        String normalizado = normalizar(valor);
        if (normalizado.length() == 11 && normalizado.chars().allMatch(Character::isDigit)) {
            if (!cpfValido(normalizado)) {
                throw new ValidacaoException("CPF inválido: " + valor);
            }
            return new Documento(normalizado, TipoDocumento.CPF);
        }
        if (normalizado.length() == 14) {
            if (!cnpjValido(normalizado)) {
                throw new ValidacaoException("CNPJ inválido: " + valor);
            }
            return new Documento(normalizado, TipoDocumento.CNPJ);
        }
        throw new ValidacaoException("Documento deve ser um CPF (11 dígitos) ou CNPJ (14 caracteres): " + valor);
    }

    public String formatado() {
        if (tipo == TipoDocumento.CPF) {
            return numero.substring(0, 3) + "." + numero.substring(3, 6) + "." + numero.substring(6, 9)
                    + "-" + numero.substring(9);
        }
        return numero.substring(0, 2) + "." + numero.substring(2, 5) + "." + numero.substring(5, 8)
                + "/" + numero.substring(8, 12) + "-" + numero.substring(12);
    }

    static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Documento é obrigatório");
        }
        return valor.replaceAll("[\\s./-]", "").toUpperCase();
    }

    private static boolean cpfValido(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        int dv1 = digitoCpf(cpf, 9);
        int dv2 = digitoCpf(cpf, 10);
        return dv1 == cpf.charAt(9) - '0' && dv2 == cpf.charAt(10) - '0';
    }

    private static int digitoCpf(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean cnpjValido(String cnpj) {
        if (!cnpj.matches("[0-9A-Z]{12}[0-9]{2}") || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int dv1 = digitoCnpj(cnpj, PESOS_CNPJ_DV1);
        int dv2 = digitoCnpj(cnpj, PESOS_CNPJ_DV2);
        return dv1 == cnpj.charAt(12) - '0' && dv2 == cnpj.charAt(13) - '0';
    }

    private static int digitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            // Valor do caractere = código ASCII - 48 (dígitos mantêm seu valor; 'A' = 17, 'B' = 18...)
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
