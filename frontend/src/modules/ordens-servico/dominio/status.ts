import type { CorSelo } from '@/shared/ui'
import type { OrdemServico, SituacaoOrcamento, StatusOS } from '../tipos'

/**
 * Regras do ciclo de vida da OS no front, espelhando StatusOrdemServico do domínio.
 * Servem para a UI oferecer só as ações válidas; o back-end continua validando cada transição.
 */

export const FLUXO_STATUS: readonly StatusOS[] = [
  'RECEBIDA',
  'EM_DIAGNOSTICO',
  'AGUARDANDO_APROVACAO',
  'EM_EXECUCAO',
  'FINALIZADA',
  'ENTREGUE',
]

export const ROTULO_STATUS: Record<StatusOS, string> = {
  RECEBIDA: 'Recebida',
  EM_DIAGNOSTICO: 'Em diagnóstico',
  AGUARDANDO_APROVACAO: 'Aguardando aprovação',
  EM_EXECUCAO: 'Em execução',
  FINALIZADA: 'Finalizada',
  ENTREGUE: 'Entregue',
}

export const COR_STATUS: Record<StatusOS, CorSelo> = {
  RECEBIDA: 'cinza',
  EM_DIAGNOSTICO: 'azul',
  AGUARDANDO_APROVACAO: 'ambar',
  EM_EXECUCAO: 'violeta',
  FINALIZADA: 'verde',
  ENTREGUE: 'ciano',
}

export const ROTULO_SITUACAO_ORCAMENTO: Record<SituacaoOrcamento, string> = {
  EM_ELABORACAO: 'Em elaboração',
  AGUARDANDO_APROVACAO: 'Aguardando aprovação',
  APROVADO: 'Aprovado',
  REJEITADO: 'Rejeitado',
}

export const COR_SITUACAO_ORCAMENTO: Record<SituacaoOrcamento, CorSelo> = {
  EM_ELABORACAO: 'cinza',
  AGUARDANDO_APROVACAO: 'ambar',
  APROVADO: 'verde',
  REJEITADO: 'vermelho',
}

const TRANSICOES: Record<StatusOS, readonly StatusOS[]> = {
  RECEBIDA: ['EM_DIAGNOSTICO'],
  EM_DIAGNOSTICO: ['AGUARDANDO_APROVACAO'],
  // Aprovado -> execução; rejeitado -> finalizada sem execução
  AGUARDANDO_APROVACAO: ['EM_EXECUCAO', 'FINALIZADA'],
  EM_EXECUCAO: ['FINALIZADA'],
  FINALIZADA: ['ENTREGUE'],
  ENTREGUE: [],
}

export function proximosPermitidos(status: StatusOS): readonly StatusOS[] {
  return TRANSICOES[status]
}

export function podeTransicionar(de: StatusOS, para: StatusOS): boolean {
  return TRANSICOES[de].includes(para)
}

export function permiteAlterarItens(status: StatusOS): boolean {
  return status === 'RECEBIDA' || status === 'EM_DIAGNOSTICO'
}

export function possuiItens(os: Pick<OrdemServico, 'servicos' | 'pecas'>): boolean {
  return os.servicos.length + os.pecas.length > 0
}

export function foiRejeitada(os: Pick<OrdemServico, 'orcamento'>): boolean {
  return os.orcamento.situacao === 'REJEITADO'
}

export type ComandoOS =
  | 'iniciarDiagnostico'
  | 'enviarOrcamento'
  | 'aprovarOrcamento'
  | 'rejeitarOrcamento'
  | 'finalizar'
  | 'entregar'

export type AcaoOS = { comando: ComandoOS; rotulo: string; bloqueio?: string; destrutiva?: boolean }

/** Próximas ações que a oficina pode executar sobre a OS, na ordem de exibição. */
export function acoesDaOficina(os: OrdemServico): AcaoOS[] {
  switch (os.status) {
    case 'RECEBIDA':
      return [{ comando: 'iniciarDiagnostico', rotulo: 'Iniciar diagnóstico' }]
    case 'EM_DIAGNOSTICO':
      return [
        {
          comando: 'enviarOrcamento',
          rotulo: 'Enviar orçamento ao cliente',
          bloqueio: possuiItens(os)
            ? undefined
            : 'Inclua ao menos um serviço ou peça para enviar o orçamento.',
        },
      ]
    case 'AGUARDANDO_APROVACAO':
      return [
        { comando: 'aprovarOrcamento', rotulo: 'Registrar aprovação' },
        { comando: 'rejeitarOrcamento', rotulo: 'Registrar rejeição', destrutiva: true },
      ]
    case 'EM_EXECUCAO':
      return [{ comando: 'finalizar', rotulo: 'Finalizar execução' }]
    case 'FINALIZADA':
      return [{ comando: 'entregar', rotulo: 'Registrar entrega do veículo' }]
    case 'ENTREGUE':
      return []
  }
}

export type EtapaLinhaDoTempo = {
  status: StatusOS
  estado: 'concluida' | 'atual' | 'pendente' | 'pulada'
  em?: string
}

/** Etapas do fluxo para exibição. Uma OS rejeitada pula a execução. */
export function linhaDoTempo(
  os: Pick<OrdemServico, 'status' | 'historico' | 'orcamento'>,
): EtapaLinhaDoTempo[] {
  const indiceAtual = FLUXO_STATUS.indexOf(os.status)
  const quando = new Map(os.historico.map((h) => [h.status, h.ocorridoEm]))
  return FLUXO_STATUS.map((status, i) => {
    if (status === 'EM_EXECUCAO' && foiRejeitada(os) && !quando.has(status))
      return { status, estado: 'pulada' }
    const estado = i < indiceAtual ? 'concluida' : i === indiceAtual ? 'atual' : 'pendente'
    return { status, estado, em: quando.get(status) }
  })
}

/** Identificador curto da OS para exibição. */
export function codigoOS(id: string): string {
  return `#${id.slice(0, 8).toUpperCase()}`
}
