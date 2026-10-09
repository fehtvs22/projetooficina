package br.com.oficina.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacaoIntegrationTest extends IntegracaoBase {

    @Test
    void apisAdministrativasExigemToken() throws Exception {
        mvc.perform(get("/api/admin/clientes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/ordens-servico").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEmiteJwtQueLiberaAsApisAdministrativas() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"senha\":\"senha-de-teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEmSegundos").value(3600));

        adminGet("/api/admin/servicos").andExpect(status().isOk());
    }

    @Test
    void loginComCredenciaisInvalidas() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Usuário ou senha inválidos"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"ninguem\",\"senha\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"usuario\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void documentacaoEHealthSaoPublicos() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content -> org.assertj.core.api.Assertions
                        .assertThat(content.getResponse().getContentAsString()).contains("bearerAuth"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status", containsString("UP")));
    }

    @Test
    void rotasDesconhecidasSaoNegadas() throws Exception {
        mvc.perform(get("/qualquer-coisa")).andExpect(status().isUnauthorized());
    }
}
