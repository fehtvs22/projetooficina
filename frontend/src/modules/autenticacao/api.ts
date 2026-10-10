import { http } from '@/shared/api/http'

export type Credenciais = { usuario: string; senha: string }
export type Token = { token: string; tipo: string; expiraEmSegundos: number }

export const autenticacaoApi = {
  login: (credenciais: Credenciais) =>
    http<Token>('/api/auth/login', { method: 'POST', body: credenciais, autenticado: false }),
}
