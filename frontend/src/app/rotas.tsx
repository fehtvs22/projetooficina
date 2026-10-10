import type { ComponentType } from 'react'
import { createBrowserRouter, type RouteObject } from 'react-router'
import { RotaProtegida } from '@/modules/autenticacao/RotaProtegida'
import { ErroRota } from './ErroRota'
import { LayoutOficina } from './layouts/LayoutOficina'
import { LayoutPublico } from './layouts/LayoutPublico'

/** Cada página vira um chunk separado (code splitting por rota). */
const pagina = (carregar: () => Promise<{ default: ComponentType }>) => async () => ({
  Component: (await carregar()).default,
})

export const rotas: RouteObject[] = [
  {
    errorElement: <ErroRota />,
    children: [
      { path: '/login', lazy: pagina(() => import('@/modules/autenticacao/LoginPage')) },
      {
        element: <LayoutPublico />,
        children: [
          {
            path: '/acompanhamento',
            lazy: pagina(() => import('@/modules/acompanhamento/AcompanhamentoPage')),
          },
        ],
      },
      {
        element: <RotaProtegida />,
        children: [
          {
            element: <LayoutOficina />,
            children: [
              { index: true, lazy: pagina(() => import('@/modules/painel/PainelPage')) },
              {
                path: '/ordens-servico',
                lazy: pagina(() => import('@/modules/ordens-servico/OrdensListaPage')),
              },
              {
                path: '/ordens-servico/nova',
                lazy: pagina(() => import('@/modules/ordens-servico/NovaOrdemPage')),
              },
              {
                path: '/ordens-servico/:id',
                lazy: pagina(() => import('@/modules/ordens-servico/OrdemDetalhePage')),
              },
              { path: '/clientes', lazy: pagina(() => import('@/modules/clientes/ClientesPage')) },
              { path: '/veiculos', lazy: pagina(() => import('@/modules/veiculos/VeiculosPage')) },
              { path: '/servicos', lazy: pagina(() => import('@/modules/servicos/ServicosPage')) },
              { path: '/estoque', lazy: pagina(() => import('@/modules/estoque/EstoquePage')) },
            ],
          },
        ],
      },
    ],
  },
]

export const criarRoteador = () => createBrowserRouter(rotas)
