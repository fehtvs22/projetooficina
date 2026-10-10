import { http, querystring } from '@/shared/api/http'
import type { ComandoOS } from './dominio/status'
import type { AbrirOrdemServico, ItemSolicitado, OrdemServico, StatusOS, TempoMedioExecucao } from './tipos'

const BASE = '/api/admin/ordens-servico'

const ROTA_COMANDO: Record<Exclude<ComandoOS, 'rejeitarOrcamento'>, string> = {
  iniciarDiagnostico: 'diagnostico',
  enviarOrcamento: 'orcamento/envio',
  aprovarOrcamento: 'orcamento/aprovacao',
  finalizar: 'finalizacao',
  entregar: 'entrega',
}

export type TipoItem = 'servicos' | 'pecas'

export const ordensServicoApi = {
  listar: (status?: StatusOS) => http<OrdemServico[]>(BASE + querystring({ status })),
  detalhar: (id: string) => http<OrdemServico>(`${BASE}/${id}`),
  abrir: (dados: AbrirOrdemServico) => http<OrdemServico>(BASE, { method: 'POST', body: dados }),
  registrarDiagnostico: (id: string, diagnostico: string) =>
    http<OrdemServico>(`${BASE}/${id}/diagnostico`, { method: 'PUT', body: { diagnostico } }),
  adicionarItem: (id: string, tipo: TipoItem, item: ItemSolicitado) =>
    http<OrdemServico>(`${BASE}/${id}/${tipo}`, { method: 'POST', body: item }),
  removerItem: (id: string, tipo: TipoItem, itemId: string) =>
    http<OrdemServico>(`${BASE}/${id}/${tipo}/${itemId}`, { method: 'DELETE' }),
  executar: (id: string, comando: ComandoOS, motivo?: string) =>
    comando === 'rejeitarOrcamento'
      ? http<OrdemServico>(`${BASE}/${id}/orcamento/rejeicao`, {
          method: 'POST',
          body: { motivo: motivo || undefined },
        })
      : http<OrdemServico>(`${BASE}/${id}/${ROTA_COMANDO[comando]}`, { method: 'POST' }),
  tempoMedioExecucao: () => http<TempoMedioExecucao>('/api/admin/relatorios/tempo-medio-execucao'),
}
