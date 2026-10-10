import { Link, Outlet } from 'react-router'

export function LayoutPublico() {
  return (
    <div className="min-h-dvh">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <span className="flex items-center gap-2 font-semibold">
            <img src="/favicon.svg" alt="" className="size-7" />
            Oficina Mecânica
          </span>
          <Link to="/login" className="text-sm text-slate-500 hover:text-slate-800">
            Acesso da oficina
          </Link>
        </div>
      </header>
      <main className="px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
