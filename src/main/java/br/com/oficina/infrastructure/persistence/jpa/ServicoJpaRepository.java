package br.com.oficina.infrastructure.persistence.jpa;

import br.com.oficina.infrastructure.persistence.entity.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicoJpaRepository extends JpaRepository<ServicoEntity, UUID> {

    List<ServicoEntity> findAllByOrderByNomeAsc();
}
