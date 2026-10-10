import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { criarRoteador } from '@/app/rotas'
import { Provedores } from '@/app/provedores'
import { sessao } from '@/modules/autenticacao/sessao'
import { configurarHttp } from '@/shared/api/http'
import './index.css'

// Composição: liga o cliente HTTP à sessão (a camada shared não depende do módulo de autenticação).
configurarHttp({ obterToken: sessao.token, aoNaoAutorizado: sessao.encerrar })

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provedores>
      <RouterProvider router={criarRoteador()} />
    </Provedores>
  </StrictMode>,
)
