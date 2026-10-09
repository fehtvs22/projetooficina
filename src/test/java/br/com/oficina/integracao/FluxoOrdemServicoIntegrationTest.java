package br.com.oficina.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxos principais da Ordem de Serviço de ponta a ponta, incluindo a API pública do cliente.
 */
class FluxoOrdemServicoIntegrationTest extends IntegracaoBase {

    private ResultActions consultaCliente(String osId, String documento) throws Exception {
        return mvc.perform(get("/api/publico/ordens-servico/" + osId).header(CABECALHO_DOCUMENTO, documento));
    }

    private ResultActions respostaCliente(String osId, String documento, String corpo) throws Exception {
        return mvc.perform(post("/api/publico/ordens-servico/" + osId + "/orcamento/resposta")
                .header(CABECALHO_DOCUMENTO, documento).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String abrirOrdem(String cpf, String itensJson) throws Exception {
        return id(adminPost("/api/admin/ordens-servico", """
                {"documentoCliente":"%s","veiculo":{"placa":"%s","marca":"VW","modelo":"Gol","ano":2020},
                 "descricaoProblema":"Revisão" %s}""".formatted(cpf, novaPlaca(), itensJson))
                .andExpect(status().isCreated()));
    }

    private int estoque(String pecaId) throws Exception {
        return json(adminGet("/api/admin/pecas/" + pecaId), "$.quantidadeEstoque");
    }

    @Test
    void fluxoCompletoDaOrdemDeServico() throws Exception {
        String cpf = novoCpf();
        novoCliente(cpf);
        int estoqueOleoAntes = estoque(PECA_OLEO);

        // Abertura com serviço e insumo; orçamento gerado automaticamente
        String osId = abrirOrdem(cpf, """
                ,"servicos":[{"id":"%s","quantidade":1}],"pecas":[{"id":"%s","quantidade":4}]"""
                .formatted(SERVICO_TROCA_OLEO, PECA_OLEO));
        adminGet("/api/admin/ordens-servico/" + osId)
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.orcamento.valorTotal").value(303.60))
                .andExpect(jsonPath("$.orcamento.situacao").value("EM_ELABORACAO"));

        // Diagnóstico e inclusão de itens
        adminPost("/api/admin/ordens-servico/" + osId + "/diagnostico")
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));
        adminPut("/api/admin/ordens-servico/" + osId + "/diagnostico", "{\"diagnostico\":\"Filtro saturado\"}")
                .andExpect(jsonPath("$.diagnostico").value("Filtro saturado"));
        adminPost("/api/admin/ordens-servico/" + osId + "/pecas", "{\"id\":\"%s\",\"quantidade\":1}".formatted(PECA_FILTRO))
                .andExpect(status().isOk());
        String itemAlinhamento = json(adminPost("/api/admin/ordens-servico/" + osId + "/servicos",
                "{\"id\":\"%s\",\"quantidade\":1}".formatted(SERVICO_ALINHAMENTO)), "$.servicos[1].id");
        adminDelete("/api/admin/ordens-servico/" + osId + "/servicos/" + itemAlinhamento)
                .andExpect(jsonPath("$.orcamento.valorTotal").value(342.10));

        // Envio do orçamento e acompanhamento pelo cliente
        adminPost("/api/admin/ordens-servico/" + osId + "/orcamento/envio")
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.orcamento.situacao").value("AGUARDANDO_APROVACAO"));
        consultaCliente(osId, cpf)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.orcamento.valorTotal").value(342.10));
        mvc.perform(get("/api/publico/ordens-servico").header(CABECALHO_DOCUMENTO, cpf))
                .andExpect(jsonPath("$[*].id", hasItem(osId)));

        // Itens bloqueados após o envio
        adminPost("/api/admin/ordens-servico/" + osId + "/servicos", "{\"id\":\"%s\",\"quantidade\":1}"
                .formatted(SERVICO_ALINHAMENTO)).andExpect(status().isUnprocessableContent());

        // Aprovação pelo cliente: execução + baixa de estoque
        respostaCliente(osId, cpf, "{\"aprovado\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.orcamento.situacao").value("APROVADO"));
        org.assertj.core.api.Assertions.assertThat(estoque(PECA_OLEO)).isEqualTo(estoqueOleoAntes - 4);

        // Transição inválida e conclusão
        adminPost("/api/admin/ordens-servico/" + osId + "/entrega").andExpect(status().isUnprocessableContent());
        adminPost("/api/admin/ordens-servico/" + osId + "/finalizacao").andExpect(jsonPath("$.status").value("FINALIZADA"));
        adminPost("/api/admin/ordens-servico/" + osId + "/entrega")
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.duracaoExecucaoMinutos").value(0))
                .andExpect(jsonPath("$.historico[*].status", contains("RECEBIDA", "EM_DIAGNOSTICO",
                        "AGUARDANDO_APROVACAO", "EM_EXECUCAO", "FINALIZADA", "ENTREGUE")));

        adminGet("/api/admin/ordens-servico?status=ENTREGUE").andExpect(jsonPath("$[*].id", hasItem(osId)));
        adminGet("/api/admin/relatorios/tempo-medio-execucao")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordensConsideradas", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.porServico[*].servico", hasItem("Troca de óleo")));
    }

    @Test
    void rejeicaoDoOrcamentoPeloCliente() throws Exception {
        String cpf = novoCpf();
        novoCliente(cpf);
        String osId = abrirOrdem(cpf, ",\"servicos\":[{\"id\":\"%s\",\"quantidade\":1}]".formatted(SERVICO_TROCA_OLEO));
        adminPost("/api/admin/ordens-servico/" + osId + "/diagnostico");
        adminPost("/api/admin/ordens-servico/" + osId + "/orcamento/envio");

        respostaCliente(osId, cpf, "{\"aprovado\":false,\"motivo\":\"Vou pensar\"}")
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.orcamento.situacao").value("REJEITADO"))
                .andExpect(jsonPath("$.orcamento.motivoRejeicao").value("Vou pensar"));
        adminPost("/api/admin/ordens-servico/" + osId + "/entrega").andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void oficinaRegistraRespostaDoOrcamento() throws Exception {
        String cpf = novoCpf();
        novoCliente(cpf);
        String aprovada = abrirOrdem(cpf, ",\"servicos\":[{\"id\":\"%s\",\"quantidade\":1}]".formatted(SERVICO_TROCA_OLEO));
        String rejeitada = abrirOrdem(cpf, ",\"servicos\":[{\"id\":\"%s\",\"quantidade\":1}]".formatted(SERVICO_TROCA_OLEO));
        for (String os : List.of(aprovada, rejeitada)) {
            adminPost("/api/admin/ordens-servico/" + os + "/diagnostico");
            adminPost("/api/admin/ordens-servico/" + os + "/orcamento/envio");
        }

        adminPost("/api/admin/ordens-servico/" + aprovada + "/orcamento/aprovacao")
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
        adminPost("/api/admin/ordens-servico/" + rejeitada + "/orcamento/rejeicao", "{\"motivo\":\"Telefone\"}")
                .andExpect(jsonPath("$.status").value("FINALIZADA"));
    }

    @Test
    void aprovacaoSemEstoqueDesfazTodaATransacao() throws Exception {
        String pecaA = novaPeca(5, 0);
        String pecaB = novaPeca(1, 0);
        // A peça que ficará sem saldo deve ser processada por último (baixas ocorrem em ordem de id),
        // garantindo que a outra já tenha sido baixada antes da falha.
        boolean aAntesDeB = UUID.fromString(pecaA).compareTo(UUID.fromString(pecaB)) < 0;
        String primeira = aAntesDeB ? pecaA : pecaB;
        String ultima = aAntesDeB ? pecaB : pecaA;
        int estoquePrimeira = estoque(primeira);

        String cpf = novoCpf();
        novoCliente(cpf);
        String osId = abrirOrdem(cpf, ",\"pecas\":[{\"id\":\"%s\",\"quantidade\":1},{\"id\":\"%s\",\"quantidade\":1}]"
                .formatted(primeira, ultima));
        adminPost("/api/admin/ordens-servico/" + osId + "/diagnostico");
        adminPost("/api/admin/ordens-servico/" + osId + "/orcamento/envio").andExpect(status().isOk());

        // O saldo da última peça acaba antes da aprovação
        adminPost("/api/admin/pecas/" + ultima + "/estoque/saida",
                "{\"quantidade\":%d}".formatted(estoque(ultima))).andExpect(status().isOk());

        respostaCliente(osId, cpf, "{\"aprovado\":true}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Estoque insuficiente")));

        org.assertj.core.api.Assertions.assertThat(estoque(primeira))
                .as("a baixa da primeira peça foi revertida (rollback)").isEqualTo(estoquePrimeira);
        adminGet("/api/admin/ordens-servico/" + osId).andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));
    }

    @Test
    void clienteSoConsultaAsPropriasOrdens() throws Exception {
        String cpf = novoCpf();
        novoCliente(cpf);
        String osId = abrirOrdem(cpf, "");

        consultaCliente(osId, novoCpf()).andExpect(status().isNotFound());
        consultaCliente(osId, "documento-invalido").andExpect(status().isBadRequest());
        mvc.perform(get("/api/publico/ordens-servico/" + osId)).andExpect(status().isBadRequest());
        respostaCliente(osId, cpf, "{\"aprovado\":true}").andExpect(status().isUnprocessableContent());
    }

    @Test
    void validacoesNaAberturaDaOrdem() throws Exception {
        String cpf = novoCpf();
        novoCliente(cpf);

        adminPost("/api/admin/ordens-servico", """
                {"documentoCliente":"%s","veiculo":{"placa":"XX-1","ano":2020}}""".formatted(cpf))
                .andExpect(status().isBadRequest());
        adminPost("/api/admin/ordens-servico", """
                {"documentoCliente":"%s","veiculo":{"placa":"%s","marca":"VW","modelo":"Gol","ano":2020}}"""
                .formatted(novoCpf(), novaPlaca()))
                .andExpect(status().isNotFound());
        adminPost("/api/admin/ordens-servico", "{\"documentoCliente\":\"%s\"}".formatted(cpf))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.veiculo").exists());
        adminGet("/api/admin/ordens-servico?status=INEXISTENTE").andExpect(status().isBadRequest());
        adminGet("/api/admin/ordens-servico/" + UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    void removePecaDaOrdem() throws Exception {
        String cpf = novoCpf();
        novoCliente(cpf);
        String osId = abrirOrdem(cpf, ",\"pecas\":[{\"id\":\"%s\",\"quantidade\":1}]".formatted(PECA_FILTRO));
        String itemId = json(adminGet("/api/admin/ordens-servico/" + osId), "$.pecas[0].id");

        adminDelete("/api/admin/ordens-servico/" + osId + "/pecas/" + itemId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.valorTotal").value(0));
    }
}
