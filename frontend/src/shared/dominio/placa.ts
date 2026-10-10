/** Regras do objeto de valor Placa (padrão antigo ABC1234 ou Mercosul ABC1D23). */

const FORMATO = /^[A-Z]{3}\d[A-Z0-9]\d{2}$/

export function normalizarPlaca(valor: string): string {
  return valor.replace(/[\s-]/g, '').toUpperCase()
}

export function placaValida(valor: string): boolean {
  return FORMATO.test(normalizarPlaca(valor))
}
