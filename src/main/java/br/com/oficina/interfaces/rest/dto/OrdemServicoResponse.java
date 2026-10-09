package br.com.oficina.interfaces.rest.dto;

import br.com.oficina.application.ordemservico.OrdemServicoDetalhe;
import br.com.oficina.domain.ordemservico.ItemPeca;
import br.com.oficina.domain.ordemservico.ItemServico;
import br.com.oficina.domain.ordemservico.Orcamento;
import br.com.oficina.domain.ordemservico.OrdemServico;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrdemServicoResponse(UUID id,
                                   String status,
                                   ClienteResumo cliente,
                                   VeiculoResumo veiculo,
                                   String descricaoProblema,
                                   String diagnostico,
                                   List<Item> servicos,
                                   List<Item> pecas,
                                   OrcamentoResponse orcamento,
                                   List<Historico> historico,
                                   Instant abertaEm,
                                   Instant inicioExecucao,
                                   Instant fimExecucao,
                                   Instant entregueEm,
                                   Long duracaoExecucaoMinutos) {

    public record ClienteResumo(UUID id, String nome, String documento) {
    }

    public record VeiculoResumo(UUID id, String placa, String marca, String modelo, int ano) {
    }

    public record Item(UUID id, UUID referenciaId, String descricao, BigDecimal precoUnitario, int quantidade,
                       BigDecimal subtotal) {
    }

    public record OrcamentoResponse(BigDecimal valorServicos, BigDecimal valorPecas, BigDecimal valorTotal,
                                    String situacao, Instant enviadoEm, Instant respondidoEm, String motivoRejeicao) {
    }

    public record Historico(String status, Instant ocorridoEm) {
    }

    public static OrdemServicoResponse de(OrdemServicoDetalhe detalhe) {
        OrdemServico os = detalhe.ordemServico();
        Orcamento o = os.getOrcamento();
        return new OrdemServicoResponse(
                os.getId(),
                os.getStatus().name(),
                new ClienteResumo(detalhe.cliente().getId(), detalhe.cliente().getNome(),
                        detalhe.cliente().getDocumento().formatado()),
                new VeiculoResumo(detalhe.veiculo().getId(), detalhe.veiculo().getPlaca().valor(),
                        detalhe.veiculo().getMarca(), detalhe.veiculo().getModelo(), detalhe.veiculo().getAno()),
                os.getDescricaoProblema(),
                os.getDiagnostico(),
                os.getServicos().stream().map(OrdemServicoResponse::item).toList(),
                os.getPecas().stream().map(OrdemServicoResponse::item).toList(),
                new OrcamentoResponse(o.valorServicos().valor(), o.valorPecas().valor(), o.valorTotal().valor(),
                        o.situacao().name(), o.enviadoEm(), o.respondidoEm(), o.motivoRejeicao()),
                os.getHistorico().stream().map(h -> new Historico(h.status().name(), h.ocorridoEm())).toList(),
                os.getAbertaEm(),
                os.getInicioExecucao(),
                os.getFimExecucao(),
                os.getEntregueEm(),
                os.duracaoExecucao().map(Duration::toMinutes).orElse(null));
    }

    private static Item item(ItemServico i) {
        return new Item(i.getId(), i.getServicoId(), i.getDescricao(), i.getPrecoUnitario().valor(), i.getQuantidade(),
                i.subtotal().valor());
    }

    private static Item item(ItemPeca i) {
        return new Item(i.getId(), i.getPecaId(), i.getDescricao(), i.getPrecoUnitario().valor(), i.getQuantidade(),
                i.subtotal().valor());
    }
}
