package br.com.oficina.domain.veiculo;

import br.com.oficina.domain.shared.Assercoes;
import br.com.oficina.domain.shared.ValidacaoException;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Veículo. Referencia o cliente proprietário apenas pelo identificador.
 */
public class Veiculo {

    static final int ANO_MINIMO = 1900;

    private final UUID id;
    private final Placa placa;
    private String marca;
    private String modelo;
    private int ano;
    private UUID clienteId;

    private Veiculo(UUID id, Placa placa, String marca, String modelo, int ano, UUID clienteId) {
        this.id = Assercoes.obrigatorio(id, "Id do veículo");
        this.placa = Assercoes.obrigatorio(placa, "Placa");
        this.clienteId = Assercoes.obrigatorio(clienteId, "Cliente do veículo");
        definirDados(marca, modelo, ano);
    }

    public static Veiculo cadastrar(Placa placa, String marca, String modelo, int ano, UUID clienteId) {
        return new Veiculo(UUID.randomUUID(), placa, marca, modelo, ano, clienteId);
    }

    public static Veiculo reconstituir(UUID id, Placa placa, String marca, String modelo, int ano, UUID clienteId) {
        return new Veiculo(id, placa, marca, modelo, ano, clienteId);
    }

    public void atualizar(String marca, String modelo, int ano, UUID clienteId) {
        definirDados(marca, modelo, ano);
        this.clienteId = Assercoes.obrigatorio(clienteId, "Cliente do veículo");
    }

    public boolean pertenceA(UUID clienteId) {
        return this.clienteId.equals(clienteId);
    }

    private void definirDados(String marca, String modelo, int ano) {
        this.marca = Assercoes.textoObrigatorio(marca, "Marca");
        this.modelo = Assercoes.textoObrigatorio(modelo, "Modelo");
        int anoMaximo = Year.now().getValue() + 1;
        if (ano < ANO_MINIMO || ano > anoMaximo) {
            throw new ValidacaoException("Ano do veículo deve estar entre " + ANO_MINIMO + " e " + anoMaximo);
        }
        this.ano = ano;
    }

    public UUID getId() {
        return id;
    }

    public Placa getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getAno() {
        return ano;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Veiculo outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
