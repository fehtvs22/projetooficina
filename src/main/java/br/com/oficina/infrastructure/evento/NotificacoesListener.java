package br.com.oficina.infrastructure.evento;

import br.com.oficina.domain.estoque.EstoqueAbaixoDoMinimo;
import br.com.oficina.domain.ordemservico.evento.OrcamentoEnviado;
import br.com.oficina.domain.ordemservico.evento.StatusOrdemServicoAlterado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Políticas reativas aos eventos de domínio, executadas somente após o commit da transação.
 * No MVP as notificações são registradas em log; em produção seriam e-mail/SMS/push para o cliente.
 */
@Component
public class NotificacoesListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacoesListener.class);

    @TransactionalEventListener(fallbackExecution = true)
    public void orcamentoEnviado(OrcamentoEnviado evento) {
        LOG.info("[NOTIFICACAO] Orçamento da OS {} (R$ {}) disponível para aprovação do cliente {}",
                evento.ordemServicoId(), evento.valorTotal().valor(), evento.clienteId());
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void statusAlterado(StatusOrdemServicoAlterado evento) {
        LOG.info("[NOTIFICACAO] OS {} mudou de {} para {} (cliente {})",
                evento.ordemServicoId(), evento.anterior(), evento.atual(), evento.clienteId());
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void estoqueBaixo(EstoqueAbaixoDoMinimo evento) {
        LOG.warn("[ESTOQUE] '{}' ({}) abaixo do mínimo: {} em estoque, mínimo {}",
                evento.nome(), evento.codigo(), evento.quantidadeAtual(), evento.estoqueMinimo());
    }
}
