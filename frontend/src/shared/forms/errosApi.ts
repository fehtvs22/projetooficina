import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError } from '@/shared/api/http'

/**
 * Leva os erros por campo devolvidos pela API (propriedade "campos" do problem+json) para o formulário.
 * Retorna true se algum campo foi marcado.
 */
export function aplicarErrosDaApi<T extends FieldValues>(
  erro: unknown,
  setError: UseFormSetError<T>,
): boolean {
  if (!(erro instanceof ApiError)) return false
  const entradas = Object.entries(erro.campos)
  for (const [campo, mensagem] of entradas) {
    setError(campo as Path<T>, { type: 'server', message: mensagem })
  }
  return entradas.length > 0
}
