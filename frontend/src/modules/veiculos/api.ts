import { criarRecurso } from '@/shared/api/crud'

export type Veiculo = {
  id: string
  placa: string
  marca: string
  modelo: string
  ano: number
  clienteId: string
}
export type DadosVeiculo = Omit<Veiculo, 'id'>

export const veiculos = criarRecurso<Veiculo, DadosVeiculo>('/api/admin/veiculos', 'veiculos')
