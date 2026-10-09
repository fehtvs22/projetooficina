package br.com.oficina.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Usuário administrador criado na inicialização (credenciais vindas de variáveis de ambiente).
 */
@ConfigurationProperties("app.admin")
public record AdminProperties(String username, String password) {
}
