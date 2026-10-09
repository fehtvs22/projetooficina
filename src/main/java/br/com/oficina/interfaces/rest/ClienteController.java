package br.com.oficina.interfaces.rest;

import br.com.oficina.application.cliente.GerenciarClientesUseCase;
import br.com.oficina.interfaces.rest.config.OpenApiConfig;
import br.com.oficina.interfaces.rest.dto.ClienteRequest;
import br.com.oficina.interfaces.rest.dto.ClienteResponse;
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
@RequestMapping("/api/admin/clientes")
@Tag(name = "2. Clientes")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class ClienteController {

    private final GerenciarClientesUseCase clientes;

    public ClienteController(GerenciarClientesUseCase clientes) {
        this.clientes = clientes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um cliente (CPF ou CNPJ validado)")
    public ClienteResponse cadastrar(@Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.de(clientes.cadastrar(request.paraDados()));
    }

    @GetMapping
    @Operation(summary = "Lista clientes; filtra por CPF/CNPJ quando 'documento' é informado")
    public List<ClienteResponse> listar(@RequestParam(required = false) String documento) {
        if (documento != null && !documento.isBlank()) {
            return List.of(ClienteResponse.de(clientes.buscarPorDocumento(documento)));
        }
        return clientes.listar().stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um cliente")
    public ClienteResponse buscar(@PathVariable UUID id) {
        return ClienteResponse.de(clientes.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome e contatos do cliente (o documento não pode mudar)")
    public ClienteResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.de(clientes.atualizar(id, request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um cliente sem veículos")
    public void remover(@PathVariable UUID id) {
        clientes.remover(id);
    }
}
