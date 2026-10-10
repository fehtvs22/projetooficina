import { criarRecurso } from '@/shared/api/crud'
import type { TipoDocumento } from '@/shared/dominio/documento'

export type Cliente = {
  id: string
  nome: string
  documento: string
  tipoDocumento: TipoDocumento
  email?: string
  telefone?: string
}

export type DadosCliente = { nome: string; documento: string; email?: string; telefone?: string }

export const clientes = criarRecurso<Cliente, DadosCliente>('/api/admin/clientes', 'clientes')
