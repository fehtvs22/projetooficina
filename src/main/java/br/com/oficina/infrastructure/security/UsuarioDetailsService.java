package br.com.oficina.infrastructure.security;

import br.com.oficina.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioJpaRepository usuarios;

    public UsuarioDetailsService(UsuarioJpaRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return usuarios.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getSenhaHash())
                        .roles(u.getPerfil())
                        .disabled(!u.isAtivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
