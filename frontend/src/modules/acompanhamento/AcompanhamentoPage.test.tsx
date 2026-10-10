import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { OrdemServico } from '@/modules/ordens-servico/tipos'
import { renderizarRotas, rota } from '@/test/renderizar'
import AcompanhamentoPage from './AcompanhamentoPage'

const os: OrdemServico = {
  id: 'abcdef12-0000-0000-0000-000000000000',
  status: 'AGUARDANDO_APROVACAO',
  cliente: { id: 'c', nome: 'Maria', documento: '529.982.247-25' },
  veiculo: { id: 'v', placa: 'BRA2E19', marca: 'VW', modelo: 'Gol', ano: 2020 },
  servicos: [
    {
      id: 'i',
      referenciaId: 's',
      descricao: 'Troca de pastilhas',
      precoUnitario: 200,
      quantidade: 1,
      subtotal: 200,
    },
  ],
  pecas: [],
  orcamento: {
    valorServicos: 200,
    valorPecas: 0,
    valorTotal: 200,
    situacao: 'AGUARDANDO_APROVACAO',
    enviadoEm: '2026-10-01T12:00:00Z',
  },
  historico: [],
  abertaEm: '2026-10-01T10:00:00Z',
}

describe('acompanhamento pelo cliente', () => {
  const fetchMock = vi.fn<typeof fetch>()
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })
  afterEach(() => vi.unstubAllGlobals())

  it('consulta pelo documento e aprova o orçamento', async () => {
    fetchMock
      .mockResolvedValueOnce(Response.json([os]))
      .mockResolvedValueOnce(Response.json({ ...os, status: 'EM_EXECUCAO' }))
      .mockResolvedValue(
        Response.json([
          { ...os, status: 'EM_EXECUCAO', orcamento: { ...os.orcamento, situacao: 'APROVADO' } },
        ]),
      )
    renderizarRotas([rota('/', <AcompanhamentoPage />)], '/')

    await userEvent.type(screen.getByLabelText('CPF ou CNPJ'), '52998224725')
    expect(screen.getByLabelText('CPF ou CNPJ')).toHaveValue('529.982.247-25')
    await userEvent.click(screen.getByRole('button', { name: 'Consultar' }))

    expect(await screen.findByText('Troca de pastilhas')).toBeInTheDocument()
    const [url, init] = fetchMock.mock.calls[0]!
    expect(url).toBe('/api/publico/ordens-servico')
    expect(init?.headers).toMatchObject({ 'X-Documento-Cliente': '529.982.247-25' })
    expect(init?.headers).not.toHaveProperty('Authorization')

    await userEvent.click(screen.getByRole('button', { name: 'Aprovar orçamento' }))
    const [urlResposta, initResposta] = fetchMock.mock.calls[1]!
    expect(urlResposta).toBe(`/api/publico/ordens-servico/${os.id}/orcamento/resposta`)
    expect(JSON.parse(String(initResposta?.body))).toEqual({ aprovado: true })
    expect(await screen.findByText('Em execução', { selector: 'span' })).toBeInTheDocument()
  })

  it('não consulta com documento inválido', async () => {
    renderizarRotas([rota('/', <AcompanhamentoPage />)], '/')
    await userEvent.type(screen.getByLabelText('CPF ou CNPJ'), '12345678900')
    await userEvent.click(screen.getByRole('button', { name: 'Consultar' }))
    expect(await screen.findByText('CPF ou CNPJ inválido')).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })
})
