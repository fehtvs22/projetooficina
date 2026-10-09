package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.autenticacao.TokenAcesso;

public record TokenResponse(String token, String tipo, long expiraEmSegundos) {

    public static TokenResponse de(TokenAcesso t) {
        return new TokenResponse(t.token(), t.tipo(), t.expiraEmSegundos());
    }
}
