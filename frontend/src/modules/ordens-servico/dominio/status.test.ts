import { describe, expect, it } from 'vitest'
import type { OrdemServico, StatusOS } from '../tipos'
import {
  acoesDaOficina,
  codigoOS,
  FLUXO_STATUS,
  linhaDoTempo,
  permiteAlterarItens,
  podeTransicionar,
} from './status'

function os(parcial: Partial<OrdemServico> = {}): OrdemServico {
  return {
    id: 'a1b2c3d4-0000-0000-0000-000000000000',
    status: 'RECEBIDA',
    cliente: { id: 'c', nome: 'Maria', documento: '529.982.247-25' },
    veiculo: { id: 'v', placa: 'BRA2E19', marca: 'VW', modelo: 'Gol', ano: 2020 },
    servicos: [],
    pecas: [],
    orcamento: { valorServicos: 0, valorPecas: 0, valorTotal: 0, situacao: 'EM_ELABORACAO' },
    historico: [{ status: 'RECEBIDA', ocorridoEm: '2026-10-01T10:00:00Z' }],
    abertaEm: '2026-10-01T10:00:00Z',
    ...parcial,
  }
}

const item = {
  id: 'i',
  referenciaId: 'r',
  descricao: 'Troca de óleo',
  precoUnitario: 100,
  quantidade: 1,
  subtotal: 100,
}

describe('ciclo de vida da OS', () => {
  it.each<[StatusOS, StatusOS, boolean]>([
    ['RECEBIDA', 'EM_DIAGNOSTICO', true],
    ['RECEBIDA', 'EM_EXECUCAO', false],
    ['AGUARDANDO_APROVACAO', 'EM_EXECUCAO', true],
    ['AGUARDANDO_APROVACAO', 'FINALIZADA', true],
    ['EM_EXECUCAO', 'ENTREGUE', false],
    ['ENTREGUE', 'RECEBIDA', false],
  ])('%s -> %s permitido: %s', (de, para, esperado) => {
    expect(podeTransicionar(de, para)).toBe(esperado)
  })

  it('só permite alterar itens antes do envio do orçamento', () => {
    expect(FLUXO_STATUS.filter(permiteAlterarItens)).toEqual(['RECEBIDA', 'EM_DIAGNOSTICO'])
  })

  it('oferece uma ação por etapa e nenhuma após a entrega', () => {
    expect(acoesDaOficina(os({ status: 'RECEBIDA' })).map((a) => a.comando)).toEqual(['iniciarDiagnostico'])
    expect(acoesDaOficina(os({ status: 'AGUARDANDO_APROVACAO' })).map((a) => a.comando)).toEqual([
      'aprovarOrcamento',
      'rejeitarOrcamento',
    ])
    expect(acoesDaOficina(os({ status: 'EM_EXECUCAO' })).map((a) => a.comando)).toEqual(['finalizar'])
    expect(acoesDaOficina(os({ status: 'FINALIZADA' })).map((a) => a.comando)).toEqual(['entregar'])
    expect(acoesDaOficina(os({ status: 'ENTREGUE' }))).toEqual([])
  })

  it('bloqueia o envio de orçamento sem itens', () => {
    expect(acoesDaOficina(os({ status: 'EM_DIAGNOSTICO' }))[0]?.bloqueio).toMatch(/ao menos um/)
    expect(acoesDaOficina(os({ status: 'EM_DIAGNOSTICO', servicos: [item] }))[0]?.bloqueio).toBeUndefined()
  })

  it('monta a linha do tempo com a etapa atual', () => {
    const etapas = linhaDoTempo(os({ status: 'EM_DIAGNOSTICO' }))
    expect(etapas.map((e) => e.estado)).toEqual([
      'concluida',
      'atual',
      'pendente',
      'pendente',
      'pendente',
      'pendente',
    ])
    expect(etapas[0]?.em).toBe('2026-10-01T10:00:00Z')
  })

  it('marca a execução como pulada quando o orçamento foi rejeitado', () => {
    const rejeitada = os({
      status: 'FINALIZADA',
      orcamento: { valorServicos: 0, valorPecas: 0, valorTotal: 0, situacao: 'REJEITADO' },
    })
    expect(linhaDoTempo(rejeitada).find((e) => e.status === 'EM_EXECUCAO')?.estado).toBe('pulada')
  })

  it('gera código curto', () => {
    expect(codigoOS('a1b2c3d4-0000')).toBe('#A1B2C3D4')
  })
})
