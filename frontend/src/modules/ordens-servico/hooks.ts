import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { pecas } from '@/modules/estoque/api'
import { ordensServicoApi } from './api'
import type { OrdemServico, StatusOS } from './tipos'

export const chavesOS = {
  todas: ['ordens-servico'] as const,
  lista: (status?: StatusOS) => ['ordens-servico', 'lista', status ?? 'todas'] as const,
  item: (id: string) => ['ordens-servico', 'item', id] as const,
  tempoMedio: ['ordens-servico', 'tempo-medio'] as const,
}

export function useOrdens(status?: StatusOS) {
  return useQuery({ queryKey: chavesOS.lista(status), queryFn: () => ordensServicoApi.listar(status) })
}

export function useOrdem(id: string) {
  return useQuery({ queryKey: chavesOS.item(id), queryFn: () => ordensServicoApi.detalhar(id) })
}

export function useTempoMedioExecucao() {
  return useQuery({ queryKey: chavesOS.tempoMedio, queryFn: ordensServicoApi.tempoMedioExecucao })
}

/**
 * Toda operação sobre a OS devolve a OS atualizada: gravamos direto no cache do detalhe
 * (sem nova requisição) e invalidamos listas, relatórios e o estoque, que podem ter mudado.
 */
export function useOperacaoOS<A>(id: string, operacao: (id: string, args: A) => Promise<OrdemServico>) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (args: A) => operacao(id, args),
    onSuccess: (os) => {
      cliente.setQueryData(chavesOS.item(id), os)
      void cliente.invalidateQueries({ queryKey: chavesOS.todas, predicate: (q) => q.queryKey[1] !== 'item' })
      void cliente.invalidateQueries({ queryKey: pecas.chaves.todas })
    },
  })
}

export function useAbrirOrdem() {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: ordensServicoApi.abrir,
    onSuccess: (os) => {
      cliente.setQueryData(chavesOS.item(os.id), os)
      void cliente.invalidateQueries({ queryKey: chavesOS.todas })
    },
  })
}
