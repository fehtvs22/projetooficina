import type { UseQueryResult } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { mensagemDeErro } from '@/shared/api/http'
import { Alerta } from './Alerta'
import { Botao } from './Botao'
import { Carregando } from './Spinner'

/** Trata carregamento e erro de uma consulta de forma padronizada; renderiza children com os dados. */
export function EstadoConsulta<T>({
  consulta,
  children,
}: {
  consulta: UseQueryResult<T>
  children: (dados: T) => ReactNode
}) {
  if (consulta.isPending) return <Carregando />
  if (consulta.isError)
    return (
      <div className="p-5">
        <Alerta tipo="erro" titulo="Não foi possível carregar os dados">
          <p>{mensagemDeErro(consulta.error)}</p>
          <Botao variante="secundario" tamanho="sm" className="mt-3" onClick={() => consulta.refetch()}>
            Tentar novamente
          </Botao>
        </Alerta>
      </div>
    )
  return <>{children(consulta.data)}</>
}
