import type { ButtonHTMLAttributes } from 'react'
import { cn } from './cn'
import { Spinner } from './Spinner'

type Variante = 'primario' | 'secundario' | 'perigo' | 'sucesso' | 'fantasma'
type Tamanho = 'sm' | 'md'

const VARIANTES: Record<Variante, string> = {
  primario: 'bg-marca-600 text-white hover:bg-marca-700',
  secundario: 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50',
  perigo: 'bg-red-600 text-white hover:bg-red-700',
  sucesso: 'bg-emerald-600 text-white hover:bg-emerald-700',
  fantasma: 'text-slate-600 hover:bg-slate-100',
}

const TAMANHOS: Record<Tamanho, string> = {
  sm: 'h-8 px-3 text-sm',
  md: 'h-10 px-4 text-sm',
}

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variante?: Variante
  tamanho?: Tamanho
  carregando?: boolean
}

export function Botao({
  variante = 'primario',
  tamanho = 'md',
  carregando = false,
  disabled,
  className,
  children,
  type = 'button',
  ...props
}: Props) {
  return (
    <button
      type={type}
      disabled={disabled || carregando}
      aria-busy={carregando || undefined}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-lg font-medium whitespace-nowrap transition-colors disabled:cursor-not-allowed disabled:opacity-60',
        VARIANTES[variante],
        TAMANHOS[tamanho],
        className,
      )}
      {...props}
    >
      {carregando && <Spinner className="size-4" />}
      {children}
    </button>
  )
}
