package br.com.oficina.application.autenticacao;

import br.com.oficina.domain.shared.Assercoes;

/**
 * Caso de uso: login de usuários administrativos da oficina.
 */
public class AutenticarUsuarioUseCase {

    private final AutenticacaoPort autenticacao;

    public AutenticarUsuarioUseCase(AutenticacaoPort autenticacao) {
        this.autenticacao = autenticacao;
    }

    public TokenAcesso autenticar(String usuario, String senha) {
        Assercoes.textoObrigatorio(usuario, "Usuário");
        Assercoes.textoObrigatorio(senha, "Senha");
        return autenticacao.autenticar(usuario, senha);
    }
}
