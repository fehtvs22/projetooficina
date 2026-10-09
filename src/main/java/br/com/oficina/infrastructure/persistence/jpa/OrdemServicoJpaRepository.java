package br.com.oficina.infrastructure.persistence.jpa;

import br.com.oficina.infrastructure.persistence.entity.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    List<OrdemServicoEntity> findAllByOrderByAbertaEmAsc();

    List<OrdemServicoEntity> findByStatusOrderByAbertaEmAsc(String status);

    List<OrdemServicoEntity> findByClienteIdOrderByAbertaEmDesc(UUID clienteId);

    List<OrdemServicoEntity> findByInicioExecucaoIsNotNullAndFimExecucaoIsNotNull();

    boolean existsByVeiculoId(UUID veiculoId);
}
