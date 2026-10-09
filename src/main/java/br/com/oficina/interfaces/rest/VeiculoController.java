package br.com.oficina.interfaces.rest;

import br.com.oficina.application.veiculo.GerenciarVeiculosUseCase;
import br.com.oficina.interfaces.rest.config.OpenApiConfig;
import br.com.oficina.interfaces.rest.dto.VeiculoRequest;
import br.com.oficina.interfaces.rest.dto.VeiculoResponse;
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
@RequestMapping("/api/admin/veiculos")
@Tag(name = "3. Veículos")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class VeiculoController {

    private final GerenciarVeiculosUseCase veiculos;

    public VeiculoController(GerenciarVeiculosUseCase veiculos) {
        this.veiculos = veiculos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um veículo (placa antiga ou Mercosul validada)")
    public VeiculoResponse cadastrar(@Valid @RequestBody VeiculoRequest request) {
        return VeiculoResponse.de(veiculos.cadastrar(request.paraDados()));
    }

    @GetMapping
    @Operation(summary = "Lista veículos, opcionalmente de um cliente")
    public List<VeiculoResponse> listar(@RequestParam(required = false) UUID clienteId) {
        return veiculos.listar(clienteId).stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um veículo")
    public VeiculoResponse buscar(@PathVariable UUID id) {
        return VeiculoResponse.de(veiculos.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza marca, modelo, ano e proprietário (a placa não pode mudar)")
    public VeiculoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody VeiculoRequest request) {
        return VeiculoResponse.de(veiculos.atualizar(id, request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um veículo sem ordens de serviço")
    public void remover(@PathVariable UUID id) {
        veiculos.remover(id);
    }
}
