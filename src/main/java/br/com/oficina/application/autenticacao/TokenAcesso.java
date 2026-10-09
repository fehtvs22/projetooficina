package br.com.oficina.application.autenticacao;

public record TokenAcesso(String token, String tipo, long expiraEmSegundos) {
}
