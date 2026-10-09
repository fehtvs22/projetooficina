package br.com.oficina.infrastructure.security;

import br.com.oficina.infrastructure.persistence.entity.UsuarioEntity;
import br.com.oficina.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Cria o usuário administrador na primeira inicialização, a partir de ADMIN_USERNAME/ADMIN_PASSWORD.
 * Nenhuma senha fica versionada no código ou nas migrations.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInicialRunner.class);

    private final AdminProperties admin;
    private final UsuarioJpaRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public AdminInicialRunner(AdminProperties admin, UsuarioJpaRepository usuarios, PasswordEncoder passwordEncoder) {
        this.admin = admin;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (admin.username() == null || admin.username().isBlank()
                || admin.password() == null || admin.password().isBlank()) {
            LOG.warn("ADMIN_USERNAME/ADMIN_PASSWORD não definidos: nenhum usuário administrador foi criado");
            return;
        }
        if (usuarios.existsByUsername(admin.username())) {
            return;
        }
        usuarios.save(new UsuarioEntity(UUID.randomUUID(), admin.username(),
                passwordEncoder.encode(admin.password()), "ADMIN", true));
        LOG.info("Usuário administrador '{}' criado", admin.username());
    }
}
