import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { renderizarRotas, rota } from '@/test/renderizar'
import LoginPage from './LoginPage'
import { RotaProtegida } from './RotaProtegida'
import { sessao } from './sessao'

const rotas = [
  rota('/login', <LoginPage />),
  { element: <RotaProtegida />, children: [rota('/clientes', <h1>Área da oficina</h1>)] },
]

describe('autenticação', () => {
  const fetchMock = vi.fn<typeof fetch>()
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })
  afterEach(() => {
    vi.unstubAllGlobals()
    sessao.encerrar()
  })

  it('redireciona para o login e volta ao destino após autenticar', async () => {
    fetchMock.mockResolvedValue(Response.json({ token: 'jwt', tipo: 'Bearer', expiraEmSegundos: 3600 }))
    const { roteador } = renderizarRotas(rotas, '/clientes')

    expect(await screen.findByRole('heading', { name: 'Acesso da oficina' })).toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('Usuário'), 'admin')
    await userEvent.type(screen.getByLabelText('Senha'), 'segredo')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('heading', { name: 'Área da oficina' })).toBeInTheDocument()
    expect(roteador.state.location.pathname).toBe('/clientes')
    expect(sessao.token()).toBe('jwt')
    expect(JSON.parse(String(fetchMock.mock.calls[0]![1]?.body))).toEqual({
      usuario: 'admin',
      senha: 'segredo',
    })
  })

  it('valida campos obrigatórios sem chamar a API', async () => {
    renderizarRotas(rotas, '/login')
    await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }))
    expect(await screen.findByText('Informe o usuário')).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('mostra o erro devolvido pela API', async () => {
    fetchMock.mockResolvedValue(
      new Response(JSON.stringify({ title: 'Não autenticado', detail: 'Usuário ou senha inválidos' }), {
        status: 401,
      }),
    )
    renderizarRotas(rotas, '/login')
    await userEvent.type(await screen.findByLabelText('Usuário'), 'admin')
    await userEvent.type(screen.getByLabelText('Senha'), 'errada')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Usuário ou senha inválidos')
    expect(sessao.token()).toBeNull()
  })

  it('ignora destino externo (open redirect)', async () => {
    fetchMock.mockResolvedValue(Response.json({ token: 'jwt', tipo: 'Bearer', expiraEmSegundos: 3600 }))
    const { roteador } = renderizarRotas(
      [...rotas, rota('/', <p>início</p>)],
      '/login?destino=//malicioso.com',
    )
    await userEvent.type(await screen.findByLabelText('Usuário'), 'admin')
    await userEvent.type(screen.getByLabelText('Senha'), 'x')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    await waitFor(() => expect(roteador.state.location.pathname).toBe('/'))
  })
})
