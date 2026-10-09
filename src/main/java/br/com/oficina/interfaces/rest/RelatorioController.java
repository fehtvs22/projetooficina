package br.com.oficina.interfaces.rest;

import br.com.oficina.application.ordemservico.CalcularTempoMedioExecucaoUseCase;
import br.com.oficina.interfaces.rest.config.OpenApiConfig;
import br.com.oficina.interfaces.rest.dto.TempoMedioExecucaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/relatorios")
@Tag(name = "7. Monitoramento")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class RelatorioController {

    private final CalcularTempoMedioExecucaoUseCase tempoMedio;

    public RelatorioController(CalcularTempoMedioExecucaoUseCase tempoMedio) {
        this.tempoMedio = tempoMedio;
    }

    @GetMapping("/tempo-medio-execucao")
    @Operation(summary = "Tempo médio de execução (aprovação até finalização), geral e por serviço")
    public TempoMedioExecucaoResponse tempoMedioExecucao() {
        return TempoMedioExecucaoResponse.de(tempoMedio.calcular());
    }
}
