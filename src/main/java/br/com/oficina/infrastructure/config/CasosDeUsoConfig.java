package br.com.oficina.infrastructure.config;

import br.com.oficina.application.autenticacao.AutenticacaoPort;
import br.com.oficina.application.autenticacao.AutenticarUsuarioUseCase;
import br.com.oficina.application.cliente.GerenciarClientesUseCase;
import br.com.oficina.application.estoque.GerenciarPecasUseCase;
import br.com.oficina.application.ordemservico.AbrirOrdemServicoUseCase;
import br.com.oficina.application.ordemservico.AcompanharOrdemServicoUseCase;
import br.com.oficina.application.ordemservico.CalcularTempoMedioExecucaoUseCase;
import br.com.oficina.application.ordemservico.ConsultarOrdensServicoUseCase;
import br.com.oficina.application.ordemservico.GerenciarItensOrdemServicoUseCase;
import br.com.oficina.application.ordemservico.OrdensServico;
import br.com.oficina.application.ordemservico.ResponderOrcamentoUseCase;
import br.com.oficina.application.servico.GerenciarServicosUseCase;
import br.com.oficina.application.veiculo.GerenciarVeiculosUseCase;
import br.com.oficina.application.shared.PublicadorEventos;
import br.com.oficina.domain.cliente.ClienteRepository;
import br.com.oficina.domain.estoque.PecaRepository;
import br.com.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.oficina.domain.servico.ServicoRepository;
import br.com.oficina.domain.veiculo.VeiculoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composição (wiring) dos casos de uso. As camadas de domínio e aplicação não conhecem o Spring:
 * é aqui, na borda, que as portas recebem seus adaptadores.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    GerenciarClientesUseCase gerenciarClientesUseCase(ClienteRepository clientes, VeiculoRepository veiculos) {
        return new GerenciarClientesUseCase(clientes, veiculos);
    }

    @Bean
    GerenciarVeiculosUseCase gerenciarVeiculosUseCase(
            VeiculoRepository veiculos, ClienteRepository clientes, OrdemServicoRepository ordens) {
        return new GerenciarVeiculosUseCase(veiculos, clientes, ordens);
    }

    @Bean
    GerenciarServicosUseCase gerenciarServicosUseCase(ServicoRepository servicos) {
        return new GerenciarServicosUseCase(servicos);
    }

    @Bean
    GerenciarPecasUseCase gerenciarPecasUseCase(PecaRepository pecas, PublicadorEventos publicador, Clock relogio) {
        return new GerenciarPecasUseCase(pecas, publicador, relogio);
    }

    @Bean
    OrdensServico ordensServico(OrdemServicoRepository ordens, ClienteRepository clientes, VeiculoRepository veiculos,
                                PublicadorEventos publicador) {
        return new OrdensServico(ordens, clientes, veiculos, publicador);
    }

    @Bean
    GerenciarItensOrdemServicoUseCase gerenciarItensOrdemServicoUseCase(ServicoRepository servicos,
                                                                       PecaRepository pecas,
                                                                       OrdensServico ordensServico) {
        return new GerenciarItensOrdemServicoUseCase(servicos, pecas, ordensServico);
    }

    @Bean
    AbrirOrdemServicoUseCase abrirOrdemServicoUseCase(ClienteRepository clientes, VeiculoRepository veiculos,
                                                      GerenciarItensOrdemServicoUseCase itens,
                                                      OrdensServico ordensServico, Clock relogio) {
        return new AbrirOrdemServicoUseCase(clientes, veiculos, itens, ordensServico, relogio);
    }

    @Bean
    AcompanharOrdemServicoUseCase acompanharOrdemServicoUseCase(OrdensServico ordensServico, PecaRepository pecas,
                                                                Clock relogio) {
        return new AcompanharOrdemServicoUseCase(ordensServico, pecas, relogio);
    }

    @Bean
    ResponderOrcamentoUseCase responderOrcamentoUseCase(OrdensServico ordensServico, PecaRepository pecas,
                                                        PublicadorEventos publicador, Clock relogio) {
        return new ResponderOrcamentoUseCase(ordensServico, pecas, publicador, relogio);
    }

    @Bean
    ConsultarOrdensServicoUseCase consultarOrdensServicoUseCase(OrdemServicoRepository ordens,
                                                                ClienteRepository clientes,
                                                                OrdensServico ordensServico) {
        return new ConsultarOrdensServicoUseCase(ordens, clientes, ordensServico);
    }

    @Bean
    CalcularTempoMedioExecucaoUseCase calcularTempoMedioExecucaoUseCase(OrdemServicoRepository ordens) {
        return new CalcularTempoMedioExecucaoUseCase(ordens);
    }

    @Bean
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(AutenticacaoPort autenticacao) {
        return new AutenticarUsuarioUseCase(autenticacao);
    }
}
