import { formatarDataHora, formatarMoeda } from '@/shared/dominio/formatos'
import type { Orcamento } from '../tipos'
import { SeloOrcamento } from './SeloStatus'

export function ResumoOrcamento({ orcamento }: { orcamento: Orcamento }) {
  return (
    <div className="space-y-3 text-sm">
      <div className="flex items-center justify-between">
        <span className="text-slate-500">Situação</span>
        <SeloOrcamento situacao={orcamento.situacao} />
      </div>
      <dl className="space-y-1.5">
        <div className="flex justify-between">
          <dt className="text-slate-500">Serviços</dt>
          <dd className="tabular-nums">{formatarMoeda(orcamento.valorServicos)}</dd>
        </div>
        <div className="flex justify-between">
          <dt className="text-slate-500">Peças e insumos</dt>
          <dd className="tabular-nums">{formatarMoeda(orcamento.valorPecas)}</dd>
        </div>
        <div className="flex justify-between border-t border-slate-100 pt-2 text-base font-semibold">
          <dt>Total</dt>
          <dd className="tabular-nums">{formatarMoeda(orcamento.valorTotal)}</dd>
        </div>
      </dl>
      {(orcamento.enviadoEm || orcamento.respondidoEm) && (
        <dl className="space-y-1 border-t border-slate-100 pt-2 text-xs text-slate-500">
          {orcamento.enviadoEm && (
            <div className="flex justify-between">
              <dt>Enviado em</dt>
              <dd>{formatarDataHora(orcamento.enviadoEm)}</dd>
            </div>
          )}
          {orcamento.respondidoEm && (
            <div className="flex justify-between">
              <dt>Respondido em</dt>
              <dd>{formatarDataHora(orcamento.respondidoEm)}</dd>
            </div>
          )}
        </dl>
      )}
      {orcamento.motivoRejeicao && (
        <p className="rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">
          Motivo da rejeição: {orcamento.motivoRejeicao}
        </p>
      )}
    </div>
  )
}
