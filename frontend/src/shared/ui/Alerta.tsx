import type { ReactNode } from 'react'
import { cn } from './cn'

type Tipo = 'erro' | 'aviso' | 'info' | 'sucesso'

const ESTILOS: Record<Tipo, string> = {
  erro: 'border-red-200 bg-red-50 text-red-800',
  aviso: 'border-amber-200 bg-amber-50 text-amber-900',
  info: 'border-blue-200 bg-blue-50 text-blue-800',
  sucesso: 'border-emerald-200 bg-emerald-50 text-emerald-800',
}

export function Alerta({
  tipo = 'info',
  titulo,
  children,
}: {
  tipo?: Tipo
  titulo?: string
  children?: ReactNode
}) {
  return (
    <div
      role={tipo === 'erro' ? 'alert' : 'status'}
      className={cn('rounded-lg border px-4 py-3 text-sm', ESTILOS[tipo])}
    >
      {titulo && <p className="font-semibold">{titulo}</p>}
      {children && <div className={titulo ? 'mt-1' : undefined}>{children}</div>}
    </div>
  )
}
