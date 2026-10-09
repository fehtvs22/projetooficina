package br.com.oficina.application.autenticacao;

/**
 * Porta de saída: verifica credenciais e emite o token de acesso (implementada com Spring Security + JWT).
 */
public interface AutenticacaoPort {

    /**
     * @throws CredenciaisInvalidasException quando usuário/senha não conferem
     */
    TokenAcesso autenticar(String usuario, String senha);
}
