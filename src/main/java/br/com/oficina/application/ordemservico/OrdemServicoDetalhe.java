package br.com.oficina.application.ordemservico;

import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.veiculo.Veiculo;

/**
 * Visão de leitura da OS com os dados do cliente e do veículo.
 */
public record OrdemServicoDetalhe(OrdemServico ordemServico, Cliente cliente, Veiculo veiculo) {
}
