import { createContext, useCallback, useContext, useRef, useState, type ReactNode } from 'react'
import { Botao } from './Botao'
import { Dialogo } from './Dialogo'

type Pedido = { titulo: string; mensagem: ReactNode; confirmar?: string; perigo?: boolean }
type Confirmar = (pedido: Pedido) => Promise<boolean>

const ContextoConfirmacao = createContext<Confirmar | null>(null)

/** Substitui window.confirm por um diálogo acessível: `if (await confirmar({...})) ...` */
export function ProvedorConfirmacao({ children }: { children: ReactNode }) {
  const [pedido, setPedido] = useState<Pedido | null>(null)
  const resolver = useRef<(valor: boolean) => void>(() => {})

  const confirmar = useCallback<Confirmar>((novo) => {
    setPedido(novo)
    return new Promise((resolve) => {
      resolver.current = resolve
    })
  }, [])

  const responder = (valor: boolean) => {
    resolver.current(valor)
    setPedido(null)
  }

  return (
    <ContextoConfirmacao.Provider value={confirmar}>
      {children}
      <Dialogo
        aberto={pedido !== null}
        aoFechar={() => responder(false)}
        titulo={pedido?.titulo ?? ''}
        rodape={
          <>
            <Botao variante="secundario" onClick={() => responder(false)}>
              Cancelar
            </Botao>
            <Botao variante={pedido?.perigo ? 'perigo' : 'primario'} onClick={() => responder(true)}>
              {pedido?.confirmar ?? 'Confirmar'}
            </Botao>
          </>
        }
      >
        <div className="text-sm text-slate-600">{pedido?.mensagem}</div>
      </Dialogo>
    </ContextoConfirmacao.Provider>
  )
}

export function useConfirmacao(): Confirmar {
  const contexto = useContext(ContextoConfirmacao)
  if (!contexto) throw new Error('useConfirmacao precisa de <ProvedorConfirmacao>')
  return contexto
}
