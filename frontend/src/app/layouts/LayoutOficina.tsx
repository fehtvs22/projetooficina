import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { NavLink, Outlet } from 'react-router'
import { sessao, useSessao } from '@/modules/autenticacao/sessao'
import { cn } from '@/shared/ui'

const MENU = [
  { para: '/', rotulo: 'Painel', fim: true },
  { para: '/ordens-servico', rotulo: 'Ordens de serviço' },
  { para: '/clientes', rotulo: 'Clientes' },
  { para: '/veiculos', rotulo: 'Veículos' },
  { para: '/servicos', rotulo: 'Serviços' },
  { para: '/estoque', rotulo: 'Peças e insumos' },
]

export function LayoutOficina() {
  const usuario = useSessao()
  const queryClient = useQueryClient()
  const [menuAberto, setMenuAberto] = useState(false)

  const sair = () => {
    sessao.encerrar()
    queryClient.clear() // não deixa dados da sessão anterior em cache
  }

  return (
    <div className="min-h-dvh lg:grid lg:grid-cols-[15rem_1fr]">
      <a
        href="#conteudo"
        className="sr-only focus:not-sr-only focus:absolute focus:m-2 focus:rounded focus:bg-white focus:p-2"
      >
        Pular para o conteúdo
      </a>
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 lg:hidden">
        <Marca />
        <button
          type="button"
          onClick={() => setMenuAberto((v) => !v)}
          aria-expanded={menuAberto}
          aria-controls="menu-principal"
          className="rounded-md px-2 py-1 text-sm font-medium text-slate-600 ring-1 ring-slate-200"
        >
          Menu
        </button>
      </header>
      <aside
        id="menu-principal"
        className={cn(
          'border-r border-slate-200 bg-white lg:sticky lg:top-0 lg:flex lg:h-dvh lg:flex-col',
          menuAberto ? 'flex flex-col' : 'hidden',
        )}
      >
        <div className="hidden px-5 py-5 lg:block">
          <Marca />
        </div>
        <nav aria-label="Principal" className="flex-1 space-y-0.5 px-3 py-3 lg:py-0">
          {MENU.map((item) => (
            <NavLink
              key={item.para}
              to={item.para}
              end={item.fim}
              onClick={() => setMenuAberto(false)}
              className={({ isActive }) =>
                cn(
                  'block rounded-lg px-3 py-2 text-sm font-medium',
                  isActive
                    ? 'bg-marca-50 text-marca-700'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                )
              }
            >
              {item.rotulo}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-slate-100 px-5 py-4 text-sm">
          <p className="text-slate-500">
            Conectado como <strong className="text-slate-700">{usuario?.usuario}</strong>
          </p>
          <button type="button" onClick={sair} className="mt-1 font-medium text-marca-600 hover:underline">
            Sair
          </button>
        </div>
      </aside>
      <main id="conteudo" className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-8 sm:py-8">
        <Outlet />
      </main>
    </div>
  )
}

function Marca() {
  return (
    <span className="flex items-center gap-2 font-semibold">
      <img src="/favicon.svg" alt="" className="size-7" />
      Oficina
    </span>
  )
}
