import { describe, expect, it } from 'vitest'
import { formatarDuracao, formatarMoeda } from './formatos'

describe('formatos', () => {
  it('formata moeda em reais', () => {
    expect(formatarMoeda(1234.5).replace(/\s/g, ' ')).toBe('R$ 1.234,50')
    expect(formatarMoeda(undefined).replace(/\s/g, ' ')).toBe('R$ 0,00')
  })

  it.each([
    [undefined, '—'],
    [45, '45 min'],
    [60, '1 h'],
    [95.4, '1 h 35 min'],
    [1440, '1 d'],
    [1560, '1 d 2 h'],
  ])('formata duração %s como %s', (minutos, esperado) => {
    expect(formatarDuracao(minutos)).toBe(esperado)
  })
})
