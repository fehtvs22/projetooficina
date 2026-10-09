package br.com.oficina.infrastructure.security;

import br.com.oficina.application.autenticacao.AutenticacaoPort;
import br.com.oficina.application.autenticacao.CredenciaisInvalidasException;
import br.com.oficina.application.autenticacao.TokenAcesso;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Adaptador da porta de autenticação: valida usuário/senha (BCrypt) e emite um JWT assinado.
 */
@Component
public class JwtAutenticacaoAdapter implements AutenticacaoPort {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final JwtProperties propriedades;
    private final Clock relogio;

    public JwtAutenticacaoAdapter(AuthenticationManager authenticationManager, JwtEncoder encoder,
                                  JwtProperties propriedades, Clock relogio) {
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    @Override
    public TokenAcesso autenticar(String usuario, String senha) {
        Authentication autenticacao;
        try {
            autenticacao = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(usuario, senha));
        } catch (AuthenticationException e) {
            throw new CredenciaisInvalidasException();
        }

        List<String> perfis = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .toList();
        Instant agora = relogio.instant();
        Duration validade = Duration.ofMinutes(propriedades.expiracaoMinutos());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .subject(autenticacao.getName())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim(SecurityConfig.CLAIM_PERFIS, perfis)
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenAcesso(token, "Bearer", validade.toSeconds());
    }
}
