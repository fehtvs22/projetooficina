package br.com.oficina.infrastructure.persistence.adapter;

import br.com.oficina.domain.ordemservico.HistoricoStatus;
import br.com.oficina.domain.ordemservico.ItemPeca;
import br.com.oficina.domain.ordemservico.ItemServico;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.ordemservico.SituacaoOrcamento;
import br.com.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.oficina.domain.shared.Dinheiro;
import br.com.oficina.infrastructure.persistence.entity.HistoricoStatusEntity;
import br.com.oficina.infrastructure.persistence.entity.ItemPecaEntity;
import br.com.oficina.infrastructure.persistence.entity.ItemServicoEntity;
import br.com.oficina.infrastructure.persistence.entity.OrdemServicoEntity;
import br.com.oficina.infrastructure.persistence.jpa.OrdemServicoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Persiste o agregado OrdemServico inteiro (raiz + itens + histórico) como uma unidade.
 * Os itens são sincronizados por identificador para que o JPA gere apenas os INSERT/UPDATE/DELETE necessários.
 */
@Component
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    private final OrdemServicoJpaRepository jpa;

    public OrdemServicoRepositoryAdapter(OrdemServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrdemServico salvar(OrdemServico os) {
        OrdemServicoEntity e = jpa.findById(os.getId()).orElseGet(() -> new OrdemServicoEntity(os.getId()));
        e.setClienteId(os.getClienteId());
        e.setVeiculoId(os.getVeiculoId());
        e.setStatus(os.getStatus().name());
        e.setDescricaoProblema(os.getDescricaoProblema());
        e.setDiagnostico(os.getDiagnostico());
        e.setSituacaoOrcamento(os.getOrcamento().situacao().name());
        e.setOrcamentoEnviadoEm(os.getOrcamento().enviadoEm());
        e.setOrcamentoRespondidoEm(os.getOrcamento().respondidoEm());
        e.setMotivoRejeicao(os.getOrcamento().motivoRejeicao());
        e.setAbertaEm(os.getAbertaEm());
        e.setInicioExecucao(os.getInicioExecucao());
        e.setFimExecucao(os.getFimExecucao());
        e.setEntregueEm(os.getEntregueEm());

        sincronizar(e.getServicos(), os.getServicos(), ItemServicoEntity::getId, ItemServico::getId,
                i -> new ItemServicoEntity(i.getId(), e, i.getServicoId(), i.getDescricao(),
                        i.getPrecoUnitario().valor(), i.getQuantidade()),
                (ent, i) -> ent.setQuantidade(i.getQuantidade()));
        sincronizar(e.getPecas(), os.getPecas(), ItemPecaEntity::getId, ItemPeca::getId,
                i -> new ItemPecaEntity(i.getId(), e, i.getPecaId(), i.getDescricao(),
                        i.getPrecoUnitario().valor(), i.getQuantidade()),
                (ent, i) -> ent.setQuantidade(i.getQuantidade()));
        sincronizar(e.getHistorico(), os.getHistorico(), HistoricoStatusEntity::getId, HistoricoStatus::id,
                h -> new HistoricoStatusEntity(h.id(), e, h.status().name(), h.ocorridoEm()),
                (ent, h) -> { });

        jpa.save(e);
        return os;
    }

    /**
     * Remove as entidades que saíram do agregado, atualiza as existentes e inclui as novas.
     */
    private static <E, D> void sincronizar(List<E> entidades, List<D> itens, Function<E, UUID> idEntidade,
                                           Function<D, UUID> idItem, Function<D, E> conversor,
                                           BiConsumer<E, D> atualizar) {
        Set<UUID> idsAtuais = itens.stream().map(idItem).collect(Collectors.toSet());
        entidades.removeIf(ent -> !idsAtuais.contains(idEntidade.apply(ent)));
        Map<UUID, E> existentes = entidades.stream().collect(Collectors.toMap(idEntidade, Function.identity()));
        for (D item : itens) {
            E existente = existentes.get(idItem.apply(item));
            if (existente == null) {
                entidades.add(conversor.apply(item));
            } else {
                atualizar.accept(existente, item);
            }
        }
    }

    @Override
    public Optional<OrdemServico> buscarPorId(UUID id) {
        return jpa.findById(id).map(OrdemServicoRepositoryAdapter::paraDominio);
    }

    @Override
    public List<OrdemServico> listar(StatusOrdemServico status) {
        List<OrdemServicoEntity> entidades = status == null
                ? jpa.findAllByOrderByAbertaEmAsc()
                : jpa.findByStatusOrderByAbertaEmAsc(status.name());
        return entidades.stream().map(OrdemServicoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public List<OrdemServico> listarPorCliente(UUID clienteId) {
        return jpa.findByClienteIdOrderByAbertaEmDesc(clienteId).stream()
                .map(OrdemServicoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public List<OrdemServico> listarComExecucaoConcluida() {
        return jpa.findByInicioExecucaoIsNotNullAndFimExecucaoIsNotNull().stream()
                .map(OrdemServicoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public boolean existeParaVeiculo(UUID veiculoId) {
        return jpa.existsByVeiculoId(veiculoId);
    }

    private static OrdemServico paraDominio(OrdemServicoEntity e) {
        return OrdemServico.reconstituir()
                .id(e.getId())
                .clienteId(e.getClienteId())
                .veiculoId(e.getVeiculoId())
                .status(StatusOrdemServico.valueOf(e.getStatus()))
                .descricaoProblema(e.getDescricaoProblema())
                .diagnostico(e.getDiagnostico())
                .situacaoOrcamento(SituacaoOrcamento.valueOf(e.getSituacaoOrcamento()))
                .orcamentoEnviadoEm(e.getOrcamentoEnviadoEm())
                .orcamentoRespondidoEm(e.getOrcamentoRespondidoEm())
                .motivoRejeicao(e.getMotivoRejeicao())
                .abertaEm(e.getAbertaEm())
                .inicioExecucao(e.getInicioExecucao())
                .fimExecucao(e.getFimExecucao())
                .entregueEm(e.getEntregueEm())
                .versao(e.getVersao())
                .servicos(e.getServicos().stream().map(i -> new ItemServico(i.getId(), i.getServicoId(),
                        i.getDescricao(), Dinheiro.de(i.getPrecoUnitario()), i.getQuantidade())).toList())
                .pecas(e.getPecas().stream().map(i -> new ItemPeca(i.getId(), i.getPecaId(), i.getDescricao(),
                        Dinheiro.de(i.getPrecoUnitario()), i.getQuantidade())).toList())
                .historico(e.getHistorico().stream().map(h -> new HistoricoStatus(h.getId(),
                        StatusOrdemServico.valueOf(h.getStatus()), h.getOcorridoEm())).toList())
                .build();
    }
}
