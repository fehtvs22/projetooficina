import { cn } from '@/shared/ui'
import { formatarDataHora } from '@/shared/dominio/formatos'
import { linhaDoTempo, ROTULO_STATUS } from '../dominio/status'
import type { OrdemServico } from '../tipos'

export function LinhaDoTempo({ os }: { os: Pick<OrdemServico, 'status' | 'historico' | 'orcamento'> }) {
  const etapas = linhaDoTempo(os)
  return (
    <ol className="grid gap-3 sm:grid-cols-6 sm:gap-0" aria-label="Andamento da ordem de serviço">
      {etapas.map((etapa, i) => (
        <li
          key={etapa.status}
          className="relative flex gap-3 sm:flex-col sm:items-center sm:text-center"
          aria-current={etapa.estado === 'atual' ? 'step' : undefined}
        >
          {i > 0 && (
            <span
              aria-hidden="true"
              className={cn(
                'absolute top-3.5 right-1/2 hidden h-0.5 w-full sm:block',
                etapa.estado === 'pendente' ? 'bg-slate-200' : 'bg-marca-600',
              )}
            />
          )}
          <span
            className={cn(
              'relative z-10 flex size-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold ring-4 ring-white',
              etapa.estado === 'concluida' && 'bg-marca-600 text-white',
              etapa.estado === 'atual' &&
                'bg-white text-marca-600 ring-marca-100 outline-2 outline-marca-600',
              etapa.estado === 'pendente' && 'bg-slate-100 text-slate-400',
              etapa.estado === 'pulada' && 'bg-slate-100 text-slate-300 line-through',
            )}
          >
            {etapa.estado === 'concluida' ? '✓' : etapa.estado === 'pulada' ? '–' : i + 1}
          </span>
          <div className="sm:mt-2">
            <p
              className={cn(
                'text-sm font-medium',
                etapa.estado === 'pendente' || etapa.estado === 'pulada'
                  ? 'text-slate-400'
                  : 'text-slate-800',
              )}
            >
              {ROTULO_STATUS[etapa.status]}
            </p>
            <p className="text-xs text-slate-500">
              {etapa.estado === 'pulada'
                ? 'Orçamento rejeitado'
                : etapa.em
                  ? formatarDataHora(etapa.em)
                  : ' '}
            </p>
          </div>
        </li>
      ))}
    </ol>
  )
}
