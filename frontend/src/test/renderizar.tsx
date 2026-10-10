import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import type { ReactElement } from 'react'
import { createMemoryRouter, RouterProvider, type RouteObject } from 'react-router'
import { ProvedorConfirmacao, ProvedorNotificacoes } from '@/shared/ui'

/** Renderiza rotas com providers reais e um QueryClient isolado por teste. */
export function renderizarRotas(rotas: RouteObject[], caminho: string) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const roteador = createMemoryRouter(rotas, { initialEntries: [caminho] })
  const resultado = render(
    <QueryClientProvider client={queryClient}>
      <ProvedorNotificacoes>
        <ProvedorConfirmacao>
          <RouterProvider router={roteador} />
        </ProvedorConfirmacao>
      </ProvedorNotificacoes>
    </QueryClientProvider>,
  )
  return { ...resultado, roteador }
}

export function rota(path: string, elemento: ReactElement): RouteObject {
  return { path, element: elemento }
}
