/** Modelo de leitura da OS, espelhando OrdemServicoResponse do back-end. */

export type StatusOS =
  'RECEBIDA' | 'EM_DIAGNOSTICO' | 'AGUARDANDO_APROVACAO' | 'EM_EXECUCAO' | 'FINALIZADA' | 'ENTREGUE'

export type SituacaoOrcamento = 'EM_ELABORACAO' | 'AGUARDANDO_APROVACAO' | 'APROVADO' | 'REJEITADO'

export type ItemOS = {
  id: string
  referenciaId: string
  descricao: string
  precoUnitario: number
  quantidade: number
  subtotal: number
}

export type Orcamento = {
  valorServicos: number
  valorPecas: number
  valorTotal: number
  situacao: SituacaoOrcamento
  enviadoEm?: string
  respondidoEm?: string
  motivoRejeicao?: string
}

export type OrdemServico = {
  id: string
  status: StatusOS
  cliente: { id: string; nome: string; documento: string }
  veiculo: { id: string; placa: string; marca: string; modelo: string; ano: number }
  descricaoProblema?: string
  diagnostico?: string
  servicos: ItemOS[]
  pecas: ItemOS[]
  orcamento: Orcamento
  historico: Array<{ status: StatusOS; ocorridoEm: string }>
  abertaEm: string
  inicioExecucao?: string
  fimExecucao?: string
  entregueEm?: string
  duracaoExecucaoMinutos?: number
}

export type ItemSolicitado = { id: string; quantidade: number }

export type AbrirOrdemServico = {
  documentoCliente: string
  veiculo: { placa: string; marca?: string; modelo?: string; ano?: number }
  descricaoProblema?: string
  servicos: ItemSolicitado[]
  pecas: ItemSolicitado[]
}

export type TempoMedioExecucao = {
  ordensConsideradas: number
  mediaGeralMinutos: number
  porServico: Array<{ servicoId: string; servico: string; ordensConsideradas: number; mediaMinutos: number }>
}
