package br.com.oficina.interfaces.rest;

import br.com.oficina.application.autenticacao.AutenticarUsuarioUseCase;
import br.com.oficina.interfaces.rest.dto.LoginRequest;
import br.com.oficina.interfaces.rest.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticação", description = "Obtenção do token JWT para as APIs administrativas")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticar;

    public AuthController(AutenticarUsuarioUseCase autenticar) {
        this.autenticar = autenticar;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário administrativo e retorna o token JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.de(autenticar.autenticar(request.usuario(), request.senha()));
    }
}
