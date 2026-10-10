import { criarRecurso } from '@/shared/api/crud'

export type Servico = {
  id: string
  nome: string
  descricao?: string
  preco: number
  tempoEstimadoMinutos: number
}
export type DadosServico = Omit<Servico, 'id'>

export const servicos = criarRecurso<Servico, DadosServico>('/api/admin/servicos', 'servicos')
