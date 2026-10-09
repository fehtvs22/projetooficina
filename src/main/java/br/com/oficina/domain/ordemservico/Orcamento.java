package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.shared.Dinheiro;

import java.time.Instant;

/**
 * Objeto de valor calculado automaticamente a partir dos serviços e peças da ordem de serviço.
 */
public record Orcamento(Dinheiro valorServicos,
                        Dinheiro valorPecas,
                        Dinheiro valorTotal,
                        SituacaoOrcamento situacao,
                        Instant enviadoEm,
                        Instant respondidoEm,
                        String motivoRejeicao) {
}
