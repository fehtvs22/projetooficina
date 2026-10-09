package br.com.oficina.integracao;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: aplicação completa (REST + segurança + casos de uso + JPA + Flyway) sobre H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegracaoBase {

    static final String SERVICO_TROCA_OLEO = "8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a01";
    static final String SERVICO_ALINHAMENTO = "8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a02";
    static final String PECA_OLEO = "5c7d2e10-3b4a-4c8e-8f21-6a9b0c1d2e01";
    static final String PECA_FILTRO = "5c7d2e10-3b4a-4c8e-8f21-6a9b0c1d2e02";
    static final String CABECALHO_DOCUMENTO = "X-Documento-Cliente";

    @Autowired
    MockMvc mvc;

    private String token;

    String token() throws Exception {
        if (token == null) {
            String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"usuario\":\"admin\",\"senha\":\"senha-de-teste\"}"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            token = JsonPath.read(resposta, "$.token");
        }
        return token;
    }

    ResultActions adminGet(String url) throws Exception {
        return mvc.perform(autenticado(get(url)));
    }

    ResultActions adminPost(String url, String json) throws Exception {
        return mvc.perform(autenticado(post(url)).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions adminPost(String url) throws Exception {
        return mvc.perform(autenticado(post(url)));
    }

    ResultActions adminPut(String url, String json) throws Exception {
        return mvc.perform(autenticado(put(url)).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions adminDelete(String url) throws Exception {
        return mvc.perform(autenticado(delete(url)));
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder requisicao) throws Exception {
        return requisicao.header("Authorization", "Bearer " + token());
    }

    static String id(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
    }

    static <T> T json(ResultActions resultado, String caminho) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), caminho);
    }

    /**
     * CPF válido aleatório, para que os testes não colidam no banco compartilhado.
     */
    static String novoCpf() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        d[0] = 1 + ThreadLocalRandom.current().nextInt(9);
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) {
                soma += d[i] * (tamanho + 1 - i);
            }
            int resto = soma % 11;
            d[tamanho] = resto < 2 ? 0 : 11 - resto;
        }
        StringBuilder cpf = new StringBuilder();
        for (int digito : d) {
            cpf.append(digito);
        }
        return cpf.toString();
    }

    static String novaPlaca() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "" + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26))
                + r.nextInt(10) + (char) ('A' + r.nextInt(26)) + r.nextInt(10) + r.nextInt(10);
    }

    String novoCliente(String cpf) throws Exception {
        return id(adminPost("/api/admin/clientes",
                "{\"nome\":\"Cliente Teste\",\"documento\":\"" + cpf + "\",\"email\":\"cliente@teste.com\"}")
                .andExpect(status().isCreated()));
    }

    String novaPeca(int estoque, int minimo) throws Exception {
        String codigo = "P-" + System.nanoTime();
        return id(adminPost("/api/admin/pecas", """
                {"codigo":"%s","nome":"Peça %s","tipo":"PECA","preco":10.00,"quantidadeInicial":%d,"estoqueMinimo":%d}
                """.formatted(codigo, codigo, estoque, minimo)).andExpect(status().isCreated()));
    }
}
