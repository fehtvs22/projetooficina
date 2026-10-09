package br.com.oficina.application.fakes;

import br.com.oficina.application.shared.PublicadorEventos;
import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.estoque.Peca;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.ordemservico.OrdemServico;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.servico.ServicoRepository;
import br.com.oficina.domain.shared.EventoDominio;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import br.com.oficina.domain.veiculo.VeiculoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementações em memória das portas de saída, para testar os casos de uso sem banco nem Spring.
 */
public final class EmMemoria {

    private EmMemoria() {
    }

    abstract static class Base<T> {
        final Map<UUID, T> dados = new LinkedHashMap<>();
        private final Function<T, UUID> id;

        Base(Function<T, UUID> id) {
            this.id = id;
        }

        public T salvar(T valor) {
            dados.put(id.apply(valor), valor);
            return valor;
        }

        public Optional<T> buscarPorId(UUID chave) {
            return Optional.ofNullable(dados.get(chave));
        }

        public List<T> listar() {
            return List.copyOf(dados.values());
        }

        public void remover(UUID chave) {
            dados.remove(chave);
        }
    }

    public static class Clientes extends Base<Cliente> implements ClienteRepository {
        public Clientes() {
            super(Cliente::getId);
        }

        @Override
        public Optional<Cliente> buscarPorDocumento(Documento documento) {
            return dados.values().stream().filter(c -> c.possuiDocumento(documento)).findFirst();
        }
    }

    public static class Veiculos extends Base<Veiculo> implements VeiculoRepository {
        public Veiculos() {
            super(Veiculo::getId);
        }

        @Override
        public Optional<Veiculo> buscarPorPlaca(Placa placa) {
            return dados.values().stream().filter(v -> v.getPlaca().equals(placa)).findFirst();
        }

        @Override
        public List<Veiculo> listarPorCliente(UUID clienteId) {
            return dados.values().stream().filter(v -> v.pertenceA(clienteId)).toList();
        }
    }

    public static class Servicos extends Base<Servico> implements ServicoRepository {
        public Servicos() {
            super(Servico::getId);
        }
    }

    public static class Pecas extends Base<Peca> implements PecaRepository {
        public int bloqueios;

        public Pecas() {
            super(Peca::getId);
        }

        @Override
        public Optional<Peca> buscarPorIdParaAtualizacao(UUID id) {
            bloqueios++;
            return buscarPorId(id);
        }

        @Override
        public Optional<Peca> buscarPorCodigo(String codigo) {
            return dados.values().stream().filter(p -> p.getCodigo().equals(codigo)).findFirst();
        }
    }

    public static class Ordens extends Base<OrdemServico> implements OrdemServicoRepository {
        public Ordens() {
            super(OrdemServico::getId);
        }

        @Override
        public List<OrdemServico> listar(StatusOrdemServico status) {
            return dados.values().stream().filter(os -> status == null || os.getStatus() == status).toList();
        }

        @Override
        public List<OrdemServico> listarPorCliente(UUID clienteId) {
            return dados.values().stream().filter(os -> os.pertenceAoCliente(clienteId)).toList();
        }

        @Override
        public List<OrdemServico> listarComExecucaoConcluida() {
            return dados.values().stream().filter(os -> os.duracaoExecucao().isPresent()).toList();
        }

        @Override
        public boolean existeParaVeiculo(UUID veiculoId) {
            return dados.values().stream().anyMatch(os -> os.getVeiculoId().equals(veiculoId));
        }
    }

    public static class Eventos implements PublicadorEventos {
        public final List<EventoDominio> publicados = new ArrayList<>();

        @Override
        public void publicar(List<EventoDominio> eventos) {
            publicados.addAll(eventos);
        }

        public <E> List<E> doTipo(Class<E> tipo) {
            return publicados.stream().filter(tipo::isInstance).map(tipo::cast).toList();
        }
    }
}
