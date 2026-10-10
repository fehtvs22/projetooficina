import { useSyncExternalStore } from 'react'

/**
 * Sessão do usuário administrativo.
 *
 * O token fica em sessionStorage (some ao fechar a aba, não é compartilhado entre abas) e é descartado
 * ao expirar. Para produção, o ideal é o back-end emitir um cookie HttpOnly + SameSite, que o JavaScript
 * não consegue ler (mitiga roubo de token por XSS).
 */

export type Sessao = { token: string; usuario: string; expiraEm: number }

const CHAVE = 'oficina.sessao'
const ouvintes = new Set<() => void>()
let atual: Sessao | null = ler()

function ler(): Sessao | null {
  try {
    const bruto = sessionStorage.getItem(CHAVE)
    if (!bruto) return null
    const sessao = JSON.parse(bruto) as Sessao
    return sessao.expiraEm > Date.now() ? sessao : null
  } catch {
    return null
  }
}

function definir(nova: Sessao | null) {
  atual = nova
  try {
    if (nova) sessionStorage.setItem(CHAVE, JSON.stringify(nova))
    else sessionStorage.removeItem(CHAVE)
  } catch {
    // armazenamento indisponível (modo privado): a sessão segue só em memória
  }
  ouvintes.forEach((ouvinte) => ouvinte())
}

export const sessao = {
  obter(): Sessao | null {
    if (atual && atual.expiraEm <= Date.now()) definir(null)
    return atual
  },
  token(): string | null {
    return sessao.obter()?.token ?? null
  },
  iniciar(token: string, usuario: string, expiraEmSegundos: number) {
    definir({ token, usuario, expiraEm: Date.now() + expiraEmSegundos * 1000 })
  },
  encerrar() {
    if (atual) definir(null)
  },
  assinar(ouvinte: () => void) {
    ouvintes.add(ouvinte)
    return () => ouvintes.delete(ouvinte)
  },
}

export function useSessao(): Sessao | null {
  return useSyncExternalStore(sessao.assinar, sessao.obter, () => null)
}
