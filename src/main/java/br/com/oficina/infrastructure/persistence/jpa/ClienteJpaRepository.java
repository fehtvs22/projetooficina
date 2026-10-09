package br.com.oficina.infrastructure.persistence.jpa;

import br.com.oficina.infrastructure.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID> {

    Optional<ClienteEntity> findByDocumento(String documento);

    List<ClienteEntity> findAllByOrderByNomeAsc();
}
