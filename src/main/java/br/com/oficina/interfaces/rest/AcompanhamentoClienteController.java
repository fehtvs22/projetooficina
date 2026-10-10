package br.com.oficina.interfaces.rest;

import br.com.oficina.application.ordemservico.ConsultarOrdensServicoUseCase;
import br.com.oficina.application.ordemservico.ResponderOrcamentoUseCase;
import br.com.oficina.interfaces.rest.dto.OrdemServicoResponse;
import br.com.oficina.interfaces.rest.dto.RespostaOrcamentoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API do cliente: acompanhar a OS e aprovar/rejeitar o orçamento. O cliente se identifica pelo CPF/CNPJ
 * enviado no cabeçalho, e só enxerga as próprias ordens de serviço. As respostas não devolvem nome nem
 * documento do cliente, e documento sem cadastro não é distinguido de cliente sem OS.
 */
@RestController
@RequestMapping("/api/publico/ordens-servico")
@Tag(name = "8. Acompanhamento pelo cliente", description = "Rotas abertas; o cliente informa seu CPF/CNPJ no cabeçalho X-Documento-Cliente")
public class AcompanhamentoClienteController {

    static final String CABECALHO_DOCUMENTO = "X-Documento-Cliente";

    private final ConsultarOrdensServicoUseCase consultar;
    private final ResponderOrcamentoUseCase orcamento;

    public AcompanhamentoClienteController(ConsultarOrdensServicoUseCase consultar, ResponderOrcamentoUseCase orcamento) {
        this.consultar = consultar;
        this.orcamento = orcamento;
    }

    @GetMapping
    @Operation(summary = "Lista as OS do cliente")
    public List<OrdemServicoResponse> listar(
            @Parameter(example = "529.982.247-25") @RequestHeader(CABECALHO_DOCUMENTO) String documento) {
        return consultar.listarDoCliente(documento).stream().map(d -> OrdemServicoResponse.de(d).semDadosDoCliente()).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta o andamento (status, orçamento e histórico) de uma OS")
    public OrdemServicoResponse consultar(
            @PathVariable UUID id,
            @Parameter(example = "529.982.247-25") @RequestHeader(CABECALHO_DOCUMENTO) String documento) {
        return OrdemServicoResponse.de(consultar.consultarPeloCliente(id, documento)).semDadosDoCliente();
    }

    @PostMapping("/{id}/orcamento/resposta")
    @Operation(summary = "Aprova ou rejeita o orçamento")
    public OrdemServicoResponse responder(
            @PathVariable UUID id,
            @Parameter(example = "529.982.247-25") @RequestHeader(CABECALHO_DOCUMENTO) String documento,
            @Valid @RequestBody RespostaOrcamentoRequest request) {
        return OrdemServicoResponse.de(Boolean.TRUE.equals(request.aprovado())
                ? orcamento.aprovarPeloCliente(id, documento)
                : orcamento.rejeitarPeloCliente(id, documento, request.motivo())).semDadosDoCliente();
    }
}
