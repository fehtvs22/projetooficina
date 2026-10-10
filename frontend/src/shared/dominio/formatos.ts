const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function formatarMoeda(valor: number | string | null | undefined): string {
  return moeda.format(Number(valor ?? 0))
}

export function formatarDataHora(iso: string | null | undefined): string {
  return iso ? dataHora.format(new Date(iso)) : '—'
}

export function formatarDuracao(minutos: number | null | undefined): string {
  if (minutos === null || minutos === undefined) return '—'
  const total = Math.round(minutos)
  if (total < 60) return `${total} min`
  const horas = Math.floor(total / 60)
  const resto = total % 60
  if (horas < 24) return resto ? `${horas} h ${resto} min` : `${horas} h`
  const dias = Math.floor(horas / 24)
  const horasResto = horas % 24
  return horasResto ? `${dias} d ${horasResto} h` : `${dias} d`
}
