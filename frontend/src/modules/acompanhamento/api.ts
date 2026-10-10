import { http } from '@/shared/api/http'
import type { OrdemServico } from '@/modules/ordens-servico/tipos'

/** API pública: o cliente se identifica pelo CPF/CNPJ no cabeçalho e só vê as próprias OS. */
const BASE = '/api/publico/ordens-servico'
const cabecalho = (documento: string) => ({ 'X-Documento-Cliente': documento })

export const acompanhamentoApi = {
  listar: (documento: string) =>
    http<OrdemServico[]>(BASE, { headers: cabecalho(documento), autenticado: false }),
  responder: (id: string, documento: string, aprovado: boolean, motivo?: string) =>
    http<OrdemServico>(`${BASE}/${id}/orcamento/resposta`, {
      method: 'POST',
      headers: cabecalho(documento),
      body: { aprovado, motivo: motivo || undefined },
      autenticado: false,
    }),
}
