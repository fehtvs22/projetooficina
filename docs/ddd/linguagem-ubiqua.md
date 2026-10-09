# Linguagem Ubíqua — Oficina Mecânica

Vocabulário compartilhado entre a oficina (atendentes, mecânicos, gestor) e o time de desenvolvimento.
Os termos abaixo são usados **com o mesmo nome** nas conversas, na documentação, na API e no código.

> Convenção: o código usa português para tudo o que pertence ao domínio (classes, métodos, eventos) e inglês
> apenas para os termos técnicos dos frameworks (`Repository`, `UseCase`, `Entity`, `Controller`).

## Glossário

| Termo | Definição no negócio | Onde está no código |
|---|---|---|
| **Cliente** | Pessoa física ou jurídica dona do veículo. Identificada de forma única pelo CPF ou CNPJ. | `domain.cliente.Cliente` (agregado) |
| **Documento** | CPF (11 dígitos) ou CNPJ (14 caracteres, inclusive o novo CNPJ alfanumérico). É validado pelos dígitos verificadores e não pode ser alterado depois do cadastro. | `domain.cliente.Documento` (objeto de valor) |
| **Veículo** | Automóvel atendido pela oficina, identificado pela placa. Pertence a um cliente. | `domain.veiculo.Veiculo` (agregado) |
| **Placa** | Identificação do veículo no padrão antigo (`ABC1234`) ou Mercosul (`ABC1D23`). | `domain.veiculo.Placa` (objeto de valor) |
| **Serviço** | Item do catálogo de mão de obra, com preço e tempo estimado (ex.: troca de óleo, alinhamento). | `domain.servico.Servico` (agregado) |
| **Peça** | Componente substituído no veículo (ex.: filtro de óleo, pastilha de freio). | `domain.estoque.Peca` com `TipoPeca.PECA` |
| **Insumo** | Material consumido na execução (ex.: óleo, fluido de freio). Tem o mesmo controle de estoque da peça. | `domain.estoque.Peca` com `TipoPeca.INSUMO` |
| **Estoque** | Saldo disponível de cada peça/insumo. Nunca pode ficar negativo. | `Peca.quantidadeEstoque`, `Peca.baixar()`, `Peca.repor()` |
| **Estoque mínimo** | Saldo abaixo do qual a peça precisa ser reposta; gera o alerta *Estoque abaixo do mínimo*. | `Peca.estoqueMinimo`, `EstoqueAbaixoDoMinimo` |
| **Baixa de estoque** | Saída de peças/insumos do estoque — automática na aprovação do orçamento ou manual (ajuste/perda). | `Peca.baixar()`, `ResponderOrcamentoUseCase` |
| **Reposição** | Entrada de peças/insumos no estoque (compra). | `Peca.repor()` |
| **Ordem de Serviço (OS)** | Documento central do atendimento: registra o veículo recebido, o problema relatado, os serviços e peças, o orçamento e o andamento até a entrega. | `domain.ordemservico.OrdemServico` (raiz de agregado) |
| **Abertura da OS** | Recepção do veículo: identificação do cliente pelo documento e do veículo pela placa (cadastrando-o se for a primeira visita). | `OrdemServico.abrir()`, `AbrirOrdemServicoUseCase` |
| **Item de serviço** | Serviço incluído em uma OS, com o preço **congelado** no momento da inclusão. | `domain.ordemservico.ItemServico` |
| **Item de peça** | Peça/insumo incluído em uma OS, com preço congelado e quantidade. | `domain.ordemservico.ItemPeca` |
| **Diagnóstico** | Etapa em que o mecânico avalia o veículo e define o que precisa ser feito; também o laudo escrito dessa avaliação. | `StatusOrdemServico.EM_DIAGNOSTICO`, `OrdemServico.registrarDiagnostico()` |
| **Orçamento** | Valor calculado automaticamente: soma dos serviços + soma das peças/insumos da OS. | `domain.ordemservico.Orcamento` (objeto de valor), `OrdemServico.getOrcamento()` |
| **Envio do orçamento** | Disponibilização do orçamento ao cliente para decisão. Depois do envio os itens ficam congelados. | `OrdemServico.enviarOrcamento()`, evento `OrcamentoEnviado` |
| **Aprovação do orçamento** | Autorização do cliente para executar o serviço. Inicia a execução e baixa o estoque. | `OrdemServico.aprovarOrcamento()`, evento `OrcamentoAprovado` |
| **Rejeição do orçamento** | O cliente não autoriza o serviço; a OS é finalizada sem execução e o veículo fica disponível para retirada. | `OrdemServico.rejeitarOrcamento()`, evento `OrcamentoRejeitado` |
| **Situação do orçamento** | Em elaboração → Aguardando aprovação → Aprovado / Rejeitado. | `SituacaoOrcamento` |
| **Status da OS** | Etapa do atendimento: Recebida, Em diagnóstico, Aguardando aprovação, Em execução, Finalizada, Entregue. | `StatusOrdemServico` |
| **Transição de status** | Mudança de uma etapa para a seguinte; só são permitidas as transições previstas nas regras (não é possível pular etapas nem voltar). | `StatusOrdemServico.proximosPermitidos()`, `TransicaoStatusInvalidaException` |
| **Histórico de status** | Linha do tempo com cada mudança de status e o momento em que ocorreu. | `HistoricoStatus` |
| **Execução** | Período em que o serviço está sendo realizado: da aprovação até a finalização. | `OrdemServico.inicioExecucao` / `fimExecucao` |
| **Finalização** | Conclusão da execução; o veículo está pronto para retirada. | `OrdemServico.finalizar()`, evento `OrdemServicoFinalizada` |
| **Entrega** | Devolução do veículo ao cliente; encerra a OS. | `OrdemServico.entregar()`, evento `VeiculoEntregue` |
| **Tempo médio de execução** | Média, em minutos, do tempo entre aprovação e finalização das OS executadas — geral e por serviço. | `CalculadoraTempoMedioExecucao` (serviço de domínio), `TempoMedioExecucao` |
| **Acompanhamento** | Consulta que o cliente faz do status, orçamento e histórico da própria OS, identificando-se pelo documento. | `ConsultarOrdensServicoUseCase.consultarPeloCliente()`, `/api/publico/ordens-servico` |
| **Atendente / Administrador** | Usuário interno da oficina que opera o sistema (cadastros, OS, estoque). Autentica-se com JWT. | `/api/admin/**`, `infrastructure.security` |

## Status da Ordem de Serviço

| Status | Significado | Entra quando… | Sai para… |
|---|---|---|---|
| `RECEBIDA` | Veículo recebido, OS aberta | a OS é aberta | `EM_DIAGNOSTICO` |
| `EM_DIAGNOSTICO` | Mecânico avaliando e montando o orçamento | o diagnóstico é iniciado | `AGUARDANDO_APROVACAO` |
| `AGUARDANDO_APROVACAO` | Orçamento com o cliente | o orçamento é enviado | `EM_EXECUCAO` (aprovado) ou `FINALIZADA` (rejeitado) |
| `EM_EXECUCAO` | Serviço sendo realizado | o orçamento é aprovado | `FINALIZADA` |
| `FINALIZADA` | Pronto para retirada | a execução termina **ou** o orçamento é rejeitado | `ENTREGUE` |
| `ENTREGUE` | Veículo devolvido; OS encerrada | o veículo é entregue | — (estado final) |

## Regras de negócio (invariantes)

1. O cliente é identificado **somente** por CPF/CNPJ válido; o documento é único e imutável.
2. A placa é única, segue o padrão antigo ou Mercosul e não muda depois do cadastro.
3. Um veículo já cadastrado para um cliente não pode ser usado em OS de outro cliente.
4. Serviços e peças só podem ser incluídos/removidos enquanto a OS está `RECEBIDA` ou `EM_DIAGNOSTICO`.
5. O preço de cada item é congelado na inclusão: alterar o catálogo não muda orçamentos já montados.
6. Incluir o mesmo serviço/peça novamente soma a quantidade ao item existente.
7. O orçamento só pode ser enviado em `EM_DIAGNOSTICO`, com ao menos um item, e havendo estoque para todas as peças.
8. A aprovação baixa o estoque de todas as peças **na mesma transação**: se faltar qualquer peça, nada é gravado.
9. O estoque nunca fica negativo; ao ficar abaixo do mínimo, é emitido o alerta de reposição.
10. Status só avançam pelas transições permitidas — não é possível pular nem voltar etapas.
11. O cliente só enxerga e responde orçamentos das próprias OS; documento divergente é tratado como "não encontrada".
12. O tempo médio considera apenas OS efetivamente executadas (aprovadas e finalizadas).
13. Cliente com veículos, veículo com OS, e serviço/peça já usados em OS não podem ser excluídos.
