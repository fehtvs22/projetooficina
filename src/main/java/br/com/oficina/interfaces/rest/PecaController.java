package br.com.oficina.interfaces.rest;

import br.com.oficina.application.estoque.GerenciarPecasUseCase;
import br.com.oficina.interfaces.rest.config.OpenApiConfig;
import br.com.oficina.interfaces.rest.dto.PecaRequest;
import br.com.oficina.interfaces.rest.dto.PecaResponse;
import br.com.oficina.interfaces.rest.dto.QuantidadeRequest;
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
@RequestMapping("/api/admin/pecas")
@Tag(name = "5. Peças e insumos", description = "Cadastro e controle de estoque")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class PecaController {

    private final GerenciarPecasUseCase pecas;

    public PecaController(GerenciarPecasUseCase pecas) {
        this.pecas = pecas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma peça ou insumo com saldo inicial")
    public PecaResponse cadastrar(@Valid @RequestBody PecaRequest request) {
        return PecaResponse.de(pecas.cadastrar(request.paraDados()));
    }

    @GetMapping
    @Operation(summary = "Lista peças/insumos; 'abaixoDoMinimo=true' mostra só os que precisam de reposição")
    public List<PecaResponse> listar(@RequestParam(defaultValue = "false") boolean abaixoDoMinimo) {
        return pecas.listar(abaixoDoMinimo).stream().map(PecaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma peça/insumo")
    public PecaResponse buscar(@PathVariable UUID id) {
        return PecaResponse.de(pecas.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza o cadastro (o saldo só muda pelas rotas de estoque)")
    public PecaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PecaRequest request) {
        return PecaResponse.de(pecas.atualizar(id, request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma peça/insumo que não foi usada em ordens de serviço")
    public void remover(@PathVariable UUID id) {
        pecas.remover(id);
    }

    @PostMapping("/{id}/estoque/entrada")
    @Operation(summary = "Registra entrada (reposição) no estoque")
    public PecaResponse entrada(@PathVariable UUID id, @Valid @RequestBody QuantidadeRequest request) {
        return PecaResponse.de(pecas.reporEstoque(id, request.quantidade()));
    }

    @PostMapping("/{id}/estoque/saida")
    @Operation(summary = "Registra saída manual (ajuste/perda) do estoque")
    public PecaResponse saida(@PathVariable UUID id, @Valid @RequestBody QuantidadeRequest request) {
        return PecaResponse.de(pecas.baixarEstoque(id, request.quantidade()));
    }
}
