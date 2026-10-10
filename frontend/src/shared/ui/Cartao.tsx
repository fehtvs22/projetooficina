import type { ReactNode } from 'react'
import { cn } from './cn'

type Props = {
  titulo?: ReactNode
  acoes?: ReactNode
  className?: string
  children: ReactNode
  semPadding?: boolean
}

export function Cartao({ titulo, acoes, className, children, semPadding }: Props) {
  return (
    <section className={cn('rounded-xl border border-slate-200 bg-white shadow-xs', className)}>
      {(titulo || acoes) && (
        <header className="flex items-center justify-between gap-3 border-b border-slate-100 px-5 py-3.5">
          {titulo && <h2 className="text-sm font-semibold text-slate-800">{titulo}</h2>}
          {acoes && <div className="flex items-center gap-2">{acoes}</div>}
        </header>
      )}
      <div className={semPadding ? undefined : 'p-5'}>{children}</div>
    </section>
  )
}
