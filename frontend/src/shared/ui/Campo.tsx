import {
  forwardRef,
  useId,
  type InputHTMLAttributes,
  type ReactNode,
  type SelectHTMLAttributes,
  type TextareaHTMLAttributes,
} from 'react'
import { cn } from './cn'

const BASE_ENTRADA =
  'block w-full rounded-lg border bg-white px-3 text-sm shadow-xs placeholder:text-slate-400 disabled:bg-slate-100 disabled:text-slate-500'

function classesEntrada(erro?: string) {
  return cn(BASE_ENTRADA, erro ? 'border-red-400 focus:outline-red-500' : 'border-slate-300')
}

type CampoProps = {
  rotulo: string
  erro?: string
  ajuda?: ReactNode
  className?: string
  children: (props: { id: string; 'aria-invalid'?: boolean; 'aria-describedby'?: string }) => ReactNode
}

/** Envolve um controle com rótulo, ajuda e mensagem de erro acessíveis. */
export function Campo({ rotulo, erro, ajuda, className, children }: CampoProps) {
  const id = useId()
  const idDescricao = `${id}-descricao`
  return (
    <div className={cn('space-y-1.5', className)}>
      <label htmlFor={id} className="block text-sm font-medium text-slate-700">
        {rotulo}
      </label>
      {children({
        id,
        'aria-invalid': erro ? true : undefined,
        'aria-describedby': erro || ajuda ? idDescricao : undefined,
      })}
      {erro ? (
        <p id={idDescricao} className="text-xs text-red-600">
          {erro}
        </p>
      ) : (
        ajuda && (
          <p id={idDescricao} className="text-xs text-slate-500">
            {ajuda}
          </p>
        )
      )}
    </div>
  )
}

type EntradaProps = InputHTMLAttributes<HTMLInputElement> & {
  rotulo: string
  erro?: string
  ajuda?: ReactNode
}

export const Entrada = forwardRef<HTMLInputElement, EntradaProps>(function Entrada(
  { rotulo, erro, ajuda, className, ...props },
  ref,
) {
  return (
    <Campo rotulo={rotulo} erro={erro} ajuda={ajuda} className={className}>
      {(a11y) => <input ref={ref} className={cn(classesEntrada(erro), 'h-10')} {...a11y} {...props} />}
    </Campo>
  )
})

type AreaTextoProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  rotulo: string
  erro?: string
  ajuda?: ReactNode
}

export const AreaTexto = forwardRef<HTMLTextAreaElement, AreaTextoProps>(function AreaTexto(
  { rotulo, erro, ajuda, className, rows = 3, ...props },
  ref,
) {
  return (
    <Campo rotulo={rotulo} erro={erro} ajuda={ajuda} className={className}>
      {(a11y) => (
        <textarea ref={ref} rows={rows} className={cn(classesEntrada(erro), 'py-2')} {...a11y} {...props} />
      )}
    </Campo>
  )
})

type SelecaoProps = SelectHTMLAttributes<HTMLSelectElement> & {
  rotulo: string
  erro?: string
  ajuda?: ReactNode
}

export const Selecao = forwardRef<HTMLSelectElement, SelecaoProps>(function Selecao(
  { rotulo, erro, ajuda, className, children, ...props },
  ref,
) {
  return (
    <Campo rotulo={rotulo} erro={erro} ajuda={ajuda} className={className}>
      {(a11y) => (
        <select ref={ref} className={cn(classesEntrada(erro), 'h-10')} {...a11y} {...props}>
          {children}
        </select>
      )}
    </Campo>
  )
})
