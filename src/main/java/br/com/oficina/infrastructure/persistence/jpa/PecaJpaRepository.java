package br.com.oficina.infrastructure.persistence.jpa;

import br.com.oficina.infrastructure.persistence.entity.PecaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PecaJpaRepository extends JpaRepository<PecaEntity, UUID> {

    Optional<PecaEntity> findByCodigo(String codigo);

    List<PecaEntity> findAllByOrderByNomeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PecaEntity p where p.id = :id")
    Optional<PecaEntity> findByIdForUpdate(@Param("id") UUID id);
}
