import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, configurarHttp, http, querystring } from './http'

function resposta(status: number, corpo?: unknown) {
  return new Response(corpo === undefined ? null : JSON.stringify(corpo), {
    status,
    headers: { 'Content-Type': status >= 400 ? 'application/problem+json' : 'application/json' },
  })
}

describe('http', () => {
  const fetchMock = vi.fn<typeof fetch>()
  const aoNaoAutorizado = vi.fn()

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock)
    configurarHttp({ urlBase: '', obterToken: () => 'tok', aoNaoAutorizado })
  })
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.resetAllMocks()
  })

  it('envia JSON e o token Bearer', async () => {
    fetchMock.mockResolvedValue(resposta(201, { id: '1' }))
    await expect(http('/api/x', { method: 'POST', body: { a: 1 } })).resolves.toEqual({ id: '1' })
    const [, init] = fetchMock.mock.calls[0]!
    expect(init?.headers).toMatchObject({ Authorization: 'Bearer tok', 'Content-Type': 'application/json' })
    expect(init?.body).toBe('{"a":1}')
  })

  it('não envia token em rotas públicas', async () => {
    fetchMock.mockResolvedValue(resposta(200, []))
    await http('/api/publico', { autenticado: false })
    expect(fetchMock.mock.calls[0]![1]?.headers).not.toHaveProperty('Authorization')
  })

  it('devolve undefined em 204', async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))
    await expect(http('/api/x', { method: 'DELETE' })).resolves.toBeUndefined()
  })

  it('converte problem+json em ApiError com erros por campo', async () => {
    fetchMock.mockResolvedValue(
      resposta(400, {
        title: 'Dados inválidos',
        detail: 'Um ou mais campos são inválidos',
        campos: { nome: 'obrigatório' },
      }),
    )
    const erro = await http('/api/x').catch((e: unknown) => e)
    expect(erro).toBeInstanceOf(ApiError)
    expect(erro).toMatchObject({
      status: 400,
      titulo: 'Dados inválidos',
      message: 'Um ou mais campos são inválidos',
      campos: { nome: 'obrigatório' },
    })
  })

  it('encerra a sessão em 401 de rota autenticada', async () => {
    fetchMock.mockResolvedValue(resposta(401))
    await expect(http('/api/admin/x')).rejects.toMatchObject({ status: 401 })
    expect(aoNaoAutorizado).toHaveBeenCalledOnce()
  })

  it('traduz falha de rede', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'))
    await expect(http('/api/x')).rejects.toMatchObject({ status: 0, titulo: 'Sem conexão' })
  })

  it('monta query string ignorando vazios', () => {
    expect(querystring({ status: 'RECEBIDA', a: undefined, b: '', c: false })).toBe(
      '?status=RECEBIDA&c=false',
    )
    expect(querystring({})).toBe('')
  })
})
