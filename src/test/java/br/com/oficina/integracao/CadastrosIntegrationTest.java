package br.com.oficina.integracao;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CadastrosIntegrationTest extends IntegracaoBase {

    @Test
    void crudDeClientes() throws Exception {
        String cpf = novoCpf();
        String id = novoCliente(cpf);

        adminPost("/api/admin/clientes", "{\"nome\":\"Outro\",\"documento\":\"" + cpf + "\"}")
                .andExpect(status().isConflict());
        adminGet("/api/admin/clientes/" + id).andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumento").value("CPF"))
                .andExpect(jsonPath("$.documento").value(cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                        + cpf.substring(6, 9) + "-" + cpf.substring(9)));
        adminGet("/api/admin/clientes?documento=" + cpf).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        adminGet("/api/admin/clientes").andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem(id)));
        adminPut("/api/admin/clientes/" + id, "{\"nome\":\"Nome Novo\",\"documento\":\"" + cpf + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"));
        adminDelete("/api/admin/clientes/" + id).andExpect(status().isNoContent());
        adminGet("/api/admin/clientes/" + id).andExpect(status().isNotFound());
    }

    @Test
    void cadastraClientePessoaJuridica() throws Exception {
        adminPost("/api/admin/clientes", "{\"nome\":\"Transportadora\",\"documento\":\"12.ABC.345/01DE-35\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDocumento").value("CNPJ"))
                .andExpect(jsonPath("$.documento").value("12.ABC.345/01DE-35"));
    }

    @Test
    void validacoesDeCliente() throws Exception {
        adminPost("/api/admin/clientes", "{\"nome\":\"X\",\"documento\":\"123.456.789-00\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("CPF inválido: 123.456.789-00"));
        adminPost("/api/admin/clientes", "{\"documento\":\"" + novoCpf() + "\",\"email\":\"invalido\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists());
        adminPost("/api/admin/clientes", "{json invalido").andExpect(status().isBadRequest());
        adminGet("/api/admin/clientes/nao-e-uuid").andExpect(status().isBadRequest());
    }

    @Test
    void crudDeVeiculos() throws Exception {
        String clienteId = novoCliente(novoCpf());
        String placa = novaPlaca();
        String id = id(adminPost("/api/admin/veiculos", """
                {"placa":"%s","marca":"Fiat","modelo":"Uno","ano":2015,"clienteId":"%s"}""".formatted(placa, clienteId))
                .andExpect(status().isCreated()));

        adminPost("/api/admin/veiculos", """
                {"placa":"%s","marca":"Fiat","modelo":"Uno","ano":2015,"clienteId":"%s"}""".formatted(placa, clienteId))
                .andExpect(status().isConflict());
        adminPost("/api/admin/veiculos", """
                {"placa":"12-ABCD","marca":"Fiat","modelo":"Uno","ano":2015,"clienteId":"%s"}""".formatted(clienteId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Placa inválida: use o padrão ABC1234 ou Mercosul ABC1D23"));
        adminGet("/api/admin/veiculos?clienteId=" + clienteId).andExpect(jsonPath("$", hasSize(1)));
        adminGet("/api/admin/veiculos").andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem(id)));
        adminPut("/api/admin/veiculos/" + id, """
                {"placa":"%s","marca":"Fiat","modelo":"Uno Way","ano":2016,"clienteId":"%s"}""".formatted(placa, clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Uno Way"));

        adminDelete("/api/admin/clientes/" + clienteId).andExpect(status().isUnprocessableContent());
        adminDelete("/api/admin/veiculos/" + id).andExpect(status().isNoContent());
        adminGet("/api/admin/veiculos/" + id).andExpect(status().isNotFound());
        adminDelete("/api/admin/clientes/" + clienteId).andExpect(status().isNoContent());
    }

    @Test
    void crudDeServicos() throws Exception {
        String id = id(adminPost("/api/admin/servicos",
                "{\"nome\":\"Higienização\",\"preco\":150.00,\"tempoEstimadoMinutos\":60}")
                .andExpect(status().isCreated()));

        adminPut("/api/admin/servicos/" + id,
                "{\"nome\":\"Higienização completa\",\"preco\":180.00,\"tempoEstimadoMinutos\":90}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(180.0));
        adminGet("/api/admin/servicos/" + id).andExpect(jsonPath("$.tempoEstimadoMinutos").value(90));
        adminGet("/api/admin/servicos").andExpect(jsonPath("$[*].id", hasItem(id)));
        adminPost("/api/admin/servicos", "{\"nome\":\"\",\"preco\":-1}").andExpect(status().isBadRequest());
        adminDelete("/api/admin/servicos/" + id).andExpect(status().isNoContent());
    }

    @Test
    void servicoUsadoEmOrdemNaoPodeSerRemovido() throws Exception {
        String servicoId = id(adminPost("/api/admin/servicos",
                "{\"nome\":\"Polimento\",\"preco\":200.00,\"tempoEstimadoMinutos\":120}"));
        String cpf = novoCpf();
        novoCliente(cpf);
        adminPost("/api/admin/ordens-servico", """
                {"documentoCliente":"%s","veiculo":{"placa":"%s","marca":"VW","modelo":"Gol","ano":2020},
                 "servicos":[{"id":"%s","quantidade":1}]}""".formatted(cpf, novaPlaca(), servicoId))
                .andExpect(status().isCreated());

        adminDelete("/api/admin/servicos/" + servicoId).andExpect(status().isConflict());
    }

    @Test
    void crudDePecasEControleDeEstoque() throws Exception {
        String id = novaPeca(5, 3);

        adminPost("/api/admin/pecas/" + id + "/estoque/entrada", "{\"quantidade\":10}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(15));
        adminPost("/api/admin/pecas/" + id + "/estoque/saida", "{\"quantidade\":13}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(2))
                .andExpect(jsonPath("$.abaixoDoMinimo").value(true));
        adminPost("/api/admin/pecas/" + id + "/estoque/saida", "{\"quantidade\":3}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
        adminPost("/api/admin/pecas/" + id + "/estoque/entrada", "{\"quantidade\":0}")
                .andExpect(status().isBadRequest());
        adminGet("/api/admin/pecas?abaixoDoMinimo=true").andExpect(jsonPath("$[*].id", hasItem(id)));

        String codigo = json(adminGet("/api/admin/pecas/" + id), "$.codigo");
        adminPost("/api/admin/pecas", """
                {"codigo":"%s","nome":"Dup","tipo":"PECA","preco":1,"estoqueMinimo":0}""".formatted(codigo))
                .andExpect(status().isConflict());
        adminPut("/api/admin/pecas/" + id, """
                {"codigo":"%s","nome":"Renomeada","tipo":"INSUMO","preco":12.5,"estoqueMinimo":1}""".formatted(codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("INSUMO"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(2));
        adminGet("/api/admin/pecas").andExpect(status().isOk());
        adminDelete("/api/admin/pecas/" + id).andExpect(status().isNoContent());
        adminGet("/api/admin/pecas/" + id).andExpect(status().isNotFound());
    }
}
