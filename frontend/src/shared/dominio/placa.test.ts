import { describe, expect, it } from 'vitest'
import { normalizarPlaca, placaValida } from './placa'

describe('Placa', () => {
  it.each(['ABC1234', 'BRA2E19', 'abc-1234', ' bra 2e19 '])('aceita %s', (valor) => {
    expect(placaValida(valor)).toBe(true)
  })

  it.each(['', 'AB1234', 'ABCD123', '1BC1234', 'ABC12345', 'ABC1D2E'])('rejeita "%s"', (valor) => {
    expect(placaValida(valor)).toBe(false)
  })

  it('normaliza', () => {
    expect(normalizarPlaca('bra-2e19')).toBe('BRA2E19')
  })
})
