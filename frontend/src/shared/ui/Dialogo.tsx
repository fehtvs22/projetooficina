import { useEffect, useRef, type ReactNode } from 'react'

type Props = {
  aberto: boolean
  aoFechar: () => void
  titulo: string
  children: ReactNode
  rodape?: ReactNode
  largura?: 'md' | 'lg'
}

/** Modal baseado no <dialog> nativo: foco preso, Esc e backdrop acessíveis sem biblioteca. */
export function Dialogo({ aberto, aoFechar, titulo, children, rodape, largura = 'md' }: Props) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialogo = ref.current
    if (!dialogo) return
    if (aberto && !dialogo.open) dialogo.showModal?.()
    if (!aberto && dialogo.open) dialogo.close?.()
  }, [aberto])

  return (
    <dialog
      ref={ref}
      aria-label={titulo}
      onCancel={(e) => {
        e.preventDefault()
        aoFechar()
      }}
      onClick={(e) => {
        if (e.target === ref.current) aoFechar()
      }}
      className={`m-auto w-[calc(100%-2rem)] rounded-xl bg-white p-0 shadow-xl backdrop:bg-slate-900/40 ${largura === 'lg' ? 'max-w-2xl' : 'max-w-lg'}`}
    >
      {aberto && (
        <div className="flex max-h-[85vh] flex-col">
          <header className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
            <h2 className="text-base font-semibold">{titulo}</h2>
            <button
              type="button"
              onClick={aoFechar}
              aria-label="Fechar"
              className="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            >
              <svg viewBox="0 0 20 20" className="size-5" fill="currentColor" aria-hidden="true">
                <path d="M6.28 5.22a.75.75 0 0 0-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 1 0 1.06 1.06L10 11.06l3.72 3.72a.75.75 0 1 0 1.06-1.06L11.06 10l3.72-3.72a.75.75 0 0 0-1.06-1.06L10 8.94 6.28 5.22Z" />
              </svg>
            </button>
          </header>
          <div className="overflow-y-auto px-5 py-4">{children}</div>
          {rodape && (
            <footer className="flex justify-end gap-2 border-t border-slate-100 px-5 py-3">{rodape}</footer>
          )}
        </div>
      )}
    </dialog>
  )
}
