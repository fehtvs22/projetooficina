/**
 * Cliente HTTP da aplicação. Converte respostas de erro RFC 9457 (application/problem+json) em ApiError.
 *
 * O módulo não conhece a autenticação: quem sabe obter o token e reagir a um 401 é registrado em
 * configurarHttp (inversão de dependência), mantendo shared/ independente dos módulos de negócio.
 */

export class ApiError extends Error {
  readonly status: number
  readonly titulo: string
  readonly campos: Record<string, string>

  constructor(status: number, titulo: string, detalhe: string, campos: Record<string, string> = {}) {
    super(detalhe)
    this.name = 'ApiError'
    this.status = status
    this.titulo = titulo
    this.campos = campos
  }
}

type Configuracao = {
  urlBase: string
  obterToken: () => string | null
  aoNaoAutorizado: () => void
}

const configuracao: Configuracao = {
  urlBase: import.meta.env.VITE_API_URL ?? '',
  obterToken: () => null,
  aoNaoAutorizado: () => {},
}

export function configurarHttp(parcial: Partial<Configuracao>): void {
  Object.assign(configuracao, parcial)
}

export type OpcoesRequisicao = {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  headers?: Record<string, string>
  /** Envia o token JWT (padrão true). Rotas públicas usam false. */
  autenticado?: boolean
  signal?: AbortSignal
}

export async function http<T>(caminho: string, opcoes: OpcoesRequisicao = {}): Promise<T> {
  const { method = 'GET', body, headers = {}, autenticado = true, signal } = opcoes
  const cabecalhos: Record<string, string> = {
    Accept: 'application/json, application/problem+json',
    ...headers,
  }
  if (body !== undefined) cabecalhos['Content-Type'] = 'application/json'
  if (autenticado) {
    const token = configuracao.obterToken()
    if (token) cabecalhos.Authorization = `Bearer ${token}`
  }

  let resposta: Response
  try {
    resposta = await fetch(configuracao.urlBase + caminho, {
      method,
      headers: cabecalhos,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    })
  } catch (erro) {
    if (erro instanceof DOMException && erro.name === 'AbortError') throw erro
    throw new ApiError(
      0,
      'Sem conexão',
      'Não foi possível falar com o servidor. Verifique se a API está no ar.',
    )
  }

  if (!resposta.ok) {
    if (resposta.status === 401 && autenticado) configuracao.aoNaoAutorizado()
    throw await paraApiError(resposta)
  }
  if (resposta.status === 204) return undefined as T
  const texto = await resposta.text()
  return (texto ? JSON.parse(texto) : undefined) as T
}

async function paraApiError(resposta: Response): Promise<ApiError> {
  const padrao = MENSAGENS_PADRAO[resposta.status] ?? 'Erro inesperado no servidor.'
  try {
    const problema = (await resposta.json()) as {
      title?: string
      detail?: string
      campos?: Record<string, string>
    }
    return new ApiError(resposta.status, problema.title ?? padrao, problema.detail ?? padrao, problema.campos)
  } catch {
    return new ApiError(resposta.status, padrao, padrao)
  }
}

const MENSAGENS_PADRAO: Record<number, string> = {
  400: 'Dados inválidos.',
  401: 'Sessão expirada. Entre novamente.',
  403: 'Você não tem permissão para esta operação.',
  404: 'Registro não encontrado.',
  409: 'Conflito com dados existentes.',
  422: 'Operação não permitida pelas regras do negócio.',
}

/** Monta a query string ignorando valores vazios. */
export function querystring(params?: Record<string, string | number | boolean | null | undefined>): string {
  if (!params) return ''
  const busca = new URLSearchParams()
  for (const [chave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== null && valor !== '') busca.set(chave, String(valor))
  }
  const texto = busca.toString()
  return texto ? `?${texto}` : ''
}

export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ApiError) return erro.message
  if (erro instanceof Error) return erro.message
  return 'Erro inesperado.'
}
