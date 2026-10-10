import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { cn } from './cn'

type Tipo = 'sucesso' | 'erro'
type Notificacao = { id: number; tipo: Tipo; mensagem: string }
type Notificar = { sucesso: (mensagem: string) => void; erro: (mensagem: string) => void }

const ContextoNotificacoes = createContext<Notificar | null>(null)
let proximoId = 1

export function ProvedorNotificacoes({ children }: { children: ReactNode }) {
  const [itens, setItens] = useState<Notificacao[]>([])

  const adicionar = useCallback((tipo: Tipo, mensagem: string) => {
    const id = proximoId++
    setItens((atuais) => [...atuais, { id, tipo, mensagem }])
    setTimeout(() => setItens((atuais) => atuais.filter((n) => n.id !== id)), tipo === 'erro' ? 7000 : 4000)
  }, [])

  const [api] = useState<Notificar>(() => ({
    sucesso: (m) => adicionar('sucesso', m),
    erro: (m) => adicionar('erro', m),
  }))

  return (
    <ContextoNotificacoes.Provider value={api}>
      {children}
      <div
        aria-live="polite"
        className="pointer-events-none fixed right-4 bottom-4 z-50 flex w-80 flex-col gap-2"
      >
        {itens.map((n) => (
          <div
            key={n.id}
            role={n.tipo === 'erro' ? 'alert' : 'status'}
            className={cn(
              'pointer-events-auto rounded-lg px-4 py-3 text-sm text-white shadow-lg',
              n.tipo === 'erro' ? 'bg-red-600' : 'bg-slate-800',
            )}
          >
            {n.mensagem}
          </div>
        ))}
      </div>
    </ContextoNotificacoes.Provider>
  )
}

export function useNotificacoes(): Notificar {
  const contexto = useContext(ContextoNotificacoes)
  if (!contexto) throw new Error('useNotificacoes precisa de <ProvedorNotificacoes>')
  return contexto
}
