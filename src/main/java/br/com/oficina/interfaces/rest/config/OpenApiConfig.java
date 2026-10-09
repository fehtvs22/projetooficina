package br.com.oficina.interfaces.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Oficina Mecânica - Sistema Integrado de Atendimento e Execução de Serviços")
                        .version("1.0.0")
                        .description("""
                                MVP do back-end da oficina: ordens de serviço, clientes, veículos, serviços, \
                                peças/insumos e estoque.

                                **Como autenticar:** chame `POST /api/auth/login`, copie o `token` e clique em \
                                **Authorize**. As rotas `/api/admin/**` exigem o token; as rotas `/api/publico/**` \
                                são usadas pelo cliente para acompanhar e aprovar a OS informando o CPF/CNPJ."""))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
