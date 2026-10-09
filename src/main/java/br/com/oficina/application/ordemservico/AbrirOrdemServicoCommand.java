package br.com.oficina.application.ordemservico;

import java.util.List;

/**
 * Dados para abertura da OS: cliente identificado por CPF/CNPJ, veículo (cadastrado na hora se ainda
 * não existir), descrição do problema e, opcionalmente, serviços e peças já solicitados.
 */
public record AbrirOrdemServicoCommand(String documentoCliente,
                                       Veiculo veiculo,
                                       String descricaoProblema,
                                       List<ItemSolicitado> servicos,
                                       List<ItemSolicitado> pecas) {

    public record Veiculo(String placa, String marca, String modelo, Integer ano) {
    }

    public List<ItemSolicitado> servicosOuVazio() {
        return servicos == null ? List.of() : servicos;
    }

    public List<ItemSolicitado> pecasOuVazio() {
        return pecas == null ? List.of() : pecas;
    }
}
