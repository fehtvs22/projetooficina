import { Link, useSearchParams } from 'react-router'
import { formatarDataHora, formatarMoeda } from '@/shared/dominio/formatos'
import { useFiltro } from '@/shared/hooks/useFiltro'
import { CabecalhoPagina, CampoBusca, Cartao, cn, EstadoConsulta, Vazio } from '@/shared/ui'
import { SeloStatus } from './componentes/SeloStatus'
import { codigoOS, FLUXO_STATUS, ROTULO_STATUS } from './dominio/status'
import { useOrdens } from './hooks'
import type { StatusOS } from './tipos'

function statusDaUrl(valor: string | null): StatusOS | undefined {
  return FLUXO_STATUS.find((s) => s === valor)
}

export default function OrdensListaPage() {
  const [params, setParams] = useSearchParams()
  const status = statusDaUrl(params.get('status'))
  const consulta = useOrdens(status)
  const { termo, setTermo, filtrados } = useFiltro(consulta.data, (os) => [
    codigoOS(os.id),
    os.cliente.nome,
    os.cliente.documento,
    os.veiculo.placa,
    os.veiculo.modelo,
  ])

  return (
    <>
      <CabecalhoPagina
        titulo="Ordens de serviço"
        descricao="Da recepção à entrega do veículo. As mais antigas aparecem primeiro."
        acoes={
          <Link
            to="/ordens-servico/nova"
            className="inline-flex h-10 items-center rounded-lg bg-marca-600 px-4 text-sm font-medium text-white hover:bg-marca-700"
          >
            Abrir OS
          </Link>
        }
      />
      <nav aria-label="Filtrar por status" className="mb-4 flex flex-wrap gap-1.5">
        {[undefined, ...FLUXO_STATUS].map((s) => (
          <button
            key={s ?? 'todas'}
            type="button"
            aria-pressed={status === s}
            onClick={() => setParams(s ? { status: s } : {}, { replace: true })}
            className={cn(
              'rounded-full px-3 py-1.5 text-sm font-medium transition-colors',
              status === s
                ? 'bg-slate-900 text-white'
                : 'bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-100',
            )}
          >
            {s ? ROTULO_STATUS[s] : 'Todas'}
          </button>
        ))}
      </nav>
      <Cartao
        semPadding
        titulo={<CampoBusca valor={termo} aoMudar={setTermo} placeholder="Buscar por OS, cliente ou placa" />}
      >
        <EstadoConsulta consulta={consulta}>
          {() =>
            filtrados.length === 0 ? (
              <Vazio
                titulo="Nenhuma ordem de serviço"
                descricao={status ? `Não há OS com status “${ROTULO_STATUS[status]}”.` : undefined}
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="tabela">
                  <thead>
                    <tr>
                      <th>OS</th>
                      <th>Cliente</th>
                      <th>Veículo</th>
                      <th>Status</th>
                      <th className="text-right">Orçamento</th>
                      <th>Aberta em</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtrados.map((os) => (
                      <tr key={os.id} className="hover:bg-slate-50">
                        <td>
                          <Link
                            to={`/ordens-servico/${os.id}`}
                            className="font-mono text-sm font-semibold text-marca-600 hover:underline"
                          >
                            {codigoOS(os.id)}
                          </Link>
                        </td>
                        <td>
                          <div className="font-medium">{os.cliente.nome}</div>
                          <div className="font-mono text-xs text-slate-500">{os.cliente.documento}</div>
                        </td>
                        <td>
                          <div className="font-mono font-semibold">{os.veiculo.placa}</div>
                          <div className="text-xs text-slate-500">
                            {os.veiculo.marca} {os.veiculo.modelo}
                          </div>
                        </td>
                        <td>
                          <SeloStatus status={os.status} />
                        </td>
                        <td className="text-right tabular-nums">{formatarMoeda(os.orcamento.valorTotal)}</td>
                        <td className="text-slate-600">{formatarDataHora(os.abertaEm)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          }
        </EstadoConsulta>
      </Cartao>
    </>
  )
}
