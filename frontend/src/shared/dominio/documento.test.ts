import { describe, expect, it } from 'vitest'
import { documentoValido, mascararDocumento, normalizarDocumento, tipoDocumento } from './documento'

// Mesmos casos de DocumentoTest (back-end): as duas validações precisam concordar.
describe('Documento', () => {
  it.each(['529.982.247-25', '52998224725'])('aceita CPF válido %s', (valor) => {
    expect(tipoDocumento(valor)).toBe('CPF')
  })

  it.each(['11.222.333/0001-81', '11222333000181', '12.ABC.345/01DE-35', '12abc34501de35'])(
    'aceita CNPJ válido %s',
    (valor) => {
      expect(tipoDocumento(valor)).toBe('CNPJ')
    },
  )

  it.each(['000.000.000-00', '111.111.111-11', '123.456.789-00'])('rejeita CPF inválido %s', (valor) => {
    expect(documentoValido(valor)).toBe(false)
  })

  it.each(['11.222.333/0001-82', '11.111.111/1111-11', '12.ABC.345/01DE-36', '12.ABC.345/01DE-3A'])(
    'rejeita CNPJ inválido %s',
    (valor) => {
      expect(documentoValido(valor)).toBe(false)
    },
  )

  it.each(['', '123', '1234567890123456', 'ABCDEFGHIJK'])('rejeita tamanho inválido "%s"', (valor) => {
    expect(documentoValido(valor)).toBe(false)
  })

  it('normaliza removendo máscara e colocando em maiúsculas', () => {
    expect(normalizarDocumento(' 12.abc.345/01de-35 ')).toBe('12ABC34501DE35')
  })

  it.each([
    ['5299', '529.9'],
    ['52998224725', '529.982.247-25'],
    ['11222333000181', '11.222.333/0001-81'],
    ['12ABC34501DE35', '12.ABC.345/01DE-35'],
  ])('mascara %s como %s', (entrada, esperado) => {
    expect(mascararDocumento(entrada)).toBe(esperado)
  })
})
