/**
 * Regras do objeto de valor Documento (CPF/CNPJ), espelhando br.com.oficina.domain.cliente.Documento.
 * Validar no front melhora a experiência; a fonte da verdade continua sendo o back-end.
 */

export type TipoDocumento = 'CPF' | 'CNPJ'

const PESOS_CNPJ_DV1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
const PESOS_CNPJ_DV2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]

export function normalizarDocumento(valor: string): string {
  return valor.replace(/[\s./-]/g, '').toUpperCase()
}

export function tipoDocumento(valor: string): TipoDocumento | null {
  const n = normalizarDocumento(valor)
  if (n.length === 11 && /^\d+$/.test(n)) return cpfValido(n) ? 'CPF' : null
  if (n.length === 14) return cnpjValido(n) ? 'CNPJ' : null
  return null
}

export function documentoValido(valor: string): boolean {
  return tipoDocumento(valor) !== null
}

/** Formata progressivamente enquanto o usuário digita. */
export function mascararDocumento(valor: string): string {
  const n = normalizarDocumento(valor)
    .replace(/[^0-9A-Z]/g, '')
    .slice(0, 14)
  if (n.length <= 11 && /^\d*$/.test(n)) {
    return n
      .replace(/^(\d{3})(\d)/, '$1.$2')
      .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/\.(\d{3})(\d{1,2})$/, '.$1-$2')
  }
  return n
    .replace(/^(\w{2})(\w)/, '$1.$2')
    .replace(/^(\w{2})\.(\w{3})(\w)/, '$1.$2.$3')
    .replace(/^(\w{2})\.(\w{3})\.(\w{3})(\w)/, '$1.$2.$3/$4')
    .replace(/\/(\w{4})(\w{1,2})$/, '/$1-$2')
}

function cpfValido(cpf: string): boolean {
  if (new Set(cpf).size === 1) return false
  return digitoCpf(cpf, 9) === Number(cpf[9]) && digitoCpf(cpf, 10) === Number(cpf[10])
}

function digitoCpf(cpf: string, tamanho: number): number {
  let soma = 0
  for (let i = 0; i < tamanho; i++) soma += Number(cpf[i]) * (tamanho + 1 - i)
  const resto = soma % 11
  return resto < 2 ? 0 : 11 - resto
}

function cnpjValido(cnpj: string): boolean {
  if (!/^[0-9A-Z]{12}\d{2}$/.test(cnpj) || new Set(cnpj).size === 1) return false
  return (
    digitoCnpj(cnpj, PESOS_CNPJ_DV1) === Number(cnpj[12]) &&
    digitoCnpj(cnpj, PESOS_CNPJ_DV2) === Number(cnpj[13])
  )
}

function digitoCnpj(cnpj: string, pesos: number[]): number {
  // Valor do caractere = código ASCII - 48 (dígitos mantêm o valor; 'A' = 17, 'B' = 18...)
  const soma = pesos.reduce((acc, peso, i) => acc + (cnpj.charCodeAt(i) - 48) * peso, 0)
  const resto = soma % 11
  return resto < 2 ? 0 : 11 - resto
}
