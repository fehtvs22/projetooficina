package br.com.oficina.interfaces.rest;

import br.com.oficina.application.servico.GerenciarServicosUseCase;
import br.com.oficina.interfaces.rest.config.OpenApiConfig;
import br.com.oficina.interfaces.rest.dto.ServicoRequest;
import br.com.oficina.interfaces.rest.dto.ServicoResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/servicos")
@Tag(name = "4. Serviços", description = "Catálogo de serviços (mão de obra)")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class ServicoController {

    private final GerenciarServicosUseCase servicos;

    public ServicoController(GerenciarServicosUseCase servicos) {
        this.servicos = servicos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um serviço")
    public ServicoResponse cadastrar(@Valid @RequestBody ServicoRequest request) {
        return ServicoResponse.de(servicos.cadastrar(request.paraDados()));
    }

    @GetMapping
    @Operation(summary = "Lista os serviços")
    public List<ServicoResponse> listar() {
        return servicos.listar().stream().map(ServicoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um serviço")
    public ServicoResponse buscar(@PathVariable UUID id) {
        return ServicoResponse.de(servicos.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um serviço (OS já abertas mantêm o preço da inclusão)")
    public ServicoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ServicoRequest request) {
        return ServicoResponse.de(servicos.atualizar(id, request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um serviço que não foi usado em ordens de serviço")
    public void remover(@PathVariable UUID id) {
        servicos.remover(id);
    }
}
