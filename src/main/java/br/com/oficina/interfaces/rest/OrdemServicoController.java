package br.com.oficina.interfaces.rest;

import br.com.oficina.application.ordemservico.AbrirOrdemServicoUseCase;
import br.com.oficina.application.ordemservico.AcompanharOrdemServicoUseCase;
import br.com.oficina.application.ordemservico.ConsultarOrdensServicoUseCase;
import br.com.oficina.application.ordemservico.GerenciarItensOrdemServicoUseCase;
import br.com.oficina.application.ordemservico.ResponderOrcamentoUseCase;
import br.com.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.oficina.interfaces.rest.config.OpenApiConfig;
import br.com.oficina.interfaces.rest.dto.AbrirOrdemServicoRequest;
import br.com.oficina.interfaces.rest.dto.DiagnosticoRequest;
import br.com.oficina.interfaces.rest.dto.ItemRequest;
import br.com.oficina.interfaces.rest.dto.OrdemServicoResponse;
import br.com.oficina.interfaces.rest.dto.RejeicaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/ordens-servico")
@Tag(name = "6. Ordens de serviço", description = "Fluxo da OS na oficina: abertura, itens, orçamento, execução e entrega")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class OrdemServicoController {

    private final AbrirOrdemServicoUseCase abrir;
    private final GerenciarItensOrdemServicoUseCase itens;
    private final AcompanharOrdemServicoUseCase acompanhar;
    private final ResponderOrcamentoUseCase orcamento;
    private final ConsultarOrdensServicoUseCase consultar;

    public OrdemServicoController(AbrirOrdemServicoUseCase abrir, GerenciarItensOrdemServicoUseCase itens,
                                  AcompanharOrdemServicoUseCase acompanhar, ResponderOrcamentoUseCase orcamento,
                                  ConsultarOrdensServicoUseCase consultar) {
        this.abrir = abrir;
        this.itens = itens;
        this.acompanhar = acompanhar;
        this.orcamento = orcamento;
        this.consultar = consultar;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre uma OS (status RECEBIDA) identificando o cliente por CPF/CNPJ")
    public OrdemServicoResponse abrir(@Valid @RequestBody AbrirOrdemServicoRequest request) {
        return OrdemServicoResponse.de(abrir.executar(request.paraComando()));
    }

    @GetMapping
    @Operation(summary = "Lista as OS (mais antigas primeiro), opcionalmente por status")
    public List<OrdemServicoResponse> listar(@RequestParam(required = false) StatusOrdemServico status) {
        return consultar.listar(status).stream().map(OrdemServicoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma OS com itens, orçamento e histórico de status")
    public OrdemServicoResponse detalhar(@PathVariable UUID id) {
        return OrdemServicoResponse.de(consultar.buscarPorId(id));
    }

    @PostMapping("/{id}/diagnostico")
    @Operation(summary = "Inicia o diagnóstico (RECEBIDA -> EM_DIAGNOSTICO)")
    public OrdemServicoResponse iniciarDiagnostico(@PathVariable UUID id) {
        return OrdemServicoResponse.de(acompanhar.iniciarDiagnostico(id));
    }

    @PutMapping("/{id}/diagnostico")
    @Operation(summary = "Registra o laudo do diagnóstico")
    public OrdemServicoResponse registrarDiagnostico(@PathVariable UUID id,
                                                     @Valid @RequestBody DiagnosticoRequest request) {
        return OrdemServicoResponse.de(acompanhar.registrarDiagnostico(id, request.diagnostico()));
    }

    @PostMapping("/{id}/servicos")
    @Operation(summary = "Inclui um serviço (orçamento recalculado automaticamente)")
    public OrdemServicoResponse adicionarServico(@PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
        return OrdemServicoResponse.de(itens.adicionarServico(id, request.id(), request.quantidade()));
    }

    @DeleteMapping("/{id}/servicos/{itemId}")
    @Operation(summary = "Remove um serviço da OS")
    public OrdemServicoResponse removerServico(@PathVariable UUID id, @PathVariable UUID itemId) {
        return OrdemServicoResponse.de(itens.removerServico(id, itemId));
    }

    @PostMapping("/{id}/pecas")
    @Operation(summary = "Inclui uma peça/insumo (verifica disponibilidade em estoque)")
    public OrdemServicoResponse adicionarPeca(@PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
        return OrdemServicoResponse.de(itens.adicionarPeca(id, request.id(), request.quantidade()));
    }

    @DeleteMapping("/{id}/pecas/{itemId}")
    @Operation(summary = "Remove uma peça/insumo da OS")
    public OrdemServicoResponse removerPeca(@PathVariable UUID id, @PathVariable UUID itemId) {
        return OrdemServicoResponse.de(itens.removerPeca(id, itemId));
    }

    @PostMapping("/{id}/orcamento/envio")
    @Operation(summary = "Envia o orçamento ao cliente (EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO)")
    public OrdemServicoResponse enviarOrcamento(@PathVariable UUID id) {
        return OrdemServicoResponse.de(acompanhar.enviarOrcamento(id));
    }

    @PostMapping("/{id}/orcamento/aprovacao")
    @Operation(summary = "Registra a aprovação recebida pela oficina (-> EM_EXECUCAO e baixa de estoque)")
    public OrdemServicoResponse aprovarOrcamento(@PathVariable UUID id) {
        return OrdemServicoResponse.de(orcamento.aprovar(id));
    }

    @PostMapping("/{id}/orcamento/rejeicao")
    @Operation(summary = "Registra a rejeição do orçamento (-> FINALIZADA sem execução)")
    public OrdemServicoResponse rejeitarOrcamento(@PathVariable UUID id,
                                                  @Valid @RequestBody(required = false) RejeicaoRequest request) {
        return OrdemServicoResponse.de(orcamento.rejeitar(id, request == null ? null : request.motivo()));
    }

    @PostMapping("/{id}/finalizacao")
    @Operation(summary = "Finaliza a execução (EM_EXECUCAO -> FINALIZADA)")
    public OrdemServicoResponse finalizar(@PathVariable UUID id) {
        return OrdemServicoResponse.de(acompanhar.finalizar(id));
    }

    @PostMapping("/{id}/entrega")
    @Operation(summary = "Registra a entrega do veículo (FINALIZADA -> ENTREGUE)")
    public OrdemServicoResponse entregar(@PathVariable UUID id) {
        return OrdemServicoResponse.de(acompanhar.entregar(id));
    }
}
