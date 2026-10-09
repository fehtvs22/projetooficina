package br.com.oficina.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret          chave HMAC (mínimo 32 caracteres), sempre fornecida por variável de ambiente
 * @param expiracaoMinutos validade do token
 * @param emissor         claim "iss"
 */
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String secret, long expiracaoMinutos, String emissor) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "Defina a variável de ambiente JWT_SECRET com no mínimo 32 caracteres");
        }
    }
}
