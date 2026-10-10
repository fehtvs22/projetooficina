import { useMutation, useQueryClient } from '@tanstack/react-query'
import { criarRecurso } from '@/shared/api/crud'
import { http } from '@/shared/api/http'

export type TipoPeca = 'PECA' | 'INSUMO'

export type Peca = {
  id: string
  codigo: string
  nome: string
  descricao?: string
  tipo: TipoPeca
  preco: number
  quantidadeEstoque: number
  estoqueMinimo: number
  abaixoDoMinimo: boolean
}

export type DadosPeca = {
  codigo: string
  nome: string
  descricao?: string
  tipo: TipoPeca
  preco: number
  quantidadeInicial?: number
  estoqueMinimo: number
}

export const TIPOS_PECA: Record<TipoPeca, string> = { PECA: 'Peça', INSUMO: 'Insumo' }

export const pecas = criarRecurso<Peca, DadosPeca>('/api/admin/pecas', 'pecas')

export type Movimento = 'entrada' | 'saida'

export function useMovimentarEstoque() {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: ({ id, movimento, quantidade }: { id: string; movimento: Movimento; quantidade: number }) =>
      http<Peca>(`/api/admin/pecas/${id}/estoque/${movimento}`, { method: 'POST', body: { quantidade } }),
    onSuccess: () => cliente.invalidateQueries({ queryKey: pecas.chaves.todas }),
  })
}
