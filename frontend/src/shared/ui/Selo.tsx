import type { ReactNode } from 'react'
import { cn } from './cn'

export type CorSelo = 'cinza' | 'azul' | 'ambar' | 'violeta' | 'verde' | 'vermelho' | 'ciano'

const CORES: Record<CorSelo, string> = {
  cinza: 'bg-slate-100 text-slate-700 ring-slate-500/20',
  azul: 'bg-blue-50 text-blue-700 ring-blue-600/20',
  ambar: 'bg-amber-50 text-amber-800 ring-amber-600/25',
  violeta: 'bg-violet-50 text-violet-700 ring-violet-600/20',
  verde: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  vermelho: 'bg-red-50 text-red-700 ring-red-600/20',
  ciano: 'bg-cyan-50 text-cyan-800 ring-cyan-600/20',
}

export function Selo({ cor = 'cinza', children }: { cor?: CorSelo; children: ReactNode }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium whitespace-nowrap ring-1 ring-inset',
        CORES[cor],
      )}
    >
      {children}
    </span>
  )
}
