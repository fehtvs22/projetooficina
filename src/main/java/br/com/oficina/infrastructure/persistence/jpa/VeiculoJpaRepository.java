package br.com.oficina.infrastructure.persistence.jpa;

import br.com.oficina.infrastructure.persistence.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, UUID> {

    Optional<VeiculoEntity> findByPlaca(String placa);

    List<VeiculoEntity> findByClienteIdOrderByPlacaAsc(UUID clienteId);

    List<VeiculoEntity> findAllByOrderByPlacaAsc();
}
