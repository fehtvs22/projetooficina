package br.com.oficina.infrastructure.persistence.jpa;

import br.com.oficina.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
