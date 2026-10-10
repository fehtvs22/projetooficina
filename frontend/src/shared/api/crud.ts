import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { http, querystring } from './http'

type Parametros = Record<string, string | number | boolean | undefined>

/**
 * Fábrica de API + hooks de cache para recursos com CRUD REST padrão
 * (GET lista, GET /{id}, POST, PUT /{id}, DELETE /{id}).
 */
export function criarRecurso<T extends { id: string }, D>(base: string, chave: string) {
  const api = {
    listar: (params?: Parametros) => http<T[]>(base + querystring(params)),
    buscar: (id: string) => http<T>(`${base}/${id}`),
    cadastrar: (dados: D) => http<T>(base, { method: 'POST', body: dados }),
    atualizar: (id: string, dados: D) => http<T>(`${base}/${id}`, { method: 'PUT', body: dados }),
    remover: (id: string) => http<void>(`${base}/${id}`, { method: 'DELETE' }),
  }

  const chaves = {
    todas: [chave] as const,
    lista: (params?: Parametros) => [chave, 'lista', params ?? {}] as const,
    item: (id: string) => [chave, 'item', id] as const,
  }

  function useLista(params?: Parametros, opcoes: { enabled?: boolean } = {}) {
    return useQuery({ queryKey: chaves.lista(params), queryFn: () => api.listar(params), ...opcoes })
  }

  function useItem(id: string | undefined) {
    return useQuery({ queryKey: chaves.item(id ?? ''), queryFn: () => api.buscar(id!), enabled: !!id })
  }

  function useSalvar() {
    const cliente = useQueryClient()
    return useMutation({
      mutationFn: ({ id, dados }: { id?: string; dados: D }) =>
        id ? api.atualizar(id, dados) : api.cadastrar(dados),
      onSuccess: () => cliente.invalidateQueries({ queryKey: chaves.todas }),
    })
  }

  function useRemover() {
    const cliente = useQueryClient()
    return useMutation({
      mutationFn: (id: string) => api.remover(id),
      onSuccess: () => cliente.invalidateQueries({ queryKey: chaves.todas }),
    })
  }

  return { api, chaves, useLista, useItem, useSalvar, useRemover }
}
