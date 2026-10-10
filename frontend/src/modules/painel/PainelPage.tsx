import { Link } from 'react-router'
import { pecas } from '@/modules/estoque/api'
import { SeloStatus } from '@/modules/ordens-servico/componentes/SeloStatus'
import { codigoOS, FLUXO_STATUS, ROTULO_STATUS } from '@/modules/ordens-servico/dominio/status'
import { useOrdens, useTempoMedioExecucao } from '@/modules/ordens-servico/hooks'
import { formatarDuracao } from '@/shared/dominio/formatos'
import { CabecalhoPagina, Cartao, EstadoConsulta, Selo, Vazio } from '@/shared/ui'

export default function PainelPage() {
  const ordens = useOrdens()
  const abaixoDoMinimo = pecas.useLista({ abaixoDoMinimo: true })
  const tempoMedio = useTempoMedioExecucao()

  const aguardandoAcao = ordens.data?.filter((os) => os.status !== 'ENTREGUE').slice(0, 8) ?? []

  return (
    <>
      <CabecalhoPagina
        titulo="Painel"
        descricao="Visão geral do pátio, do estoque e do desempenho da oficina."
      />

      <section
        aria-label="Ordens por status"
        className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6"
      >
        {FLUXO_STATUS.map((status) => {
          const total = ordens.data?.filter((os) => os.status === status).length
          return (
            <Link
              key={status}
              to={`/ordens-servico?status=${status}`}
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs transition-colors hover:border-marca-600"
            >
              <p className="text-xs font-medium text-slate-500">{ROTULO_STATUS[status]}</p>
              <p className="mt-1 text-2xl font-semibold tabular-nums">{total ?? '–'}</p>
            </Link>
          )
        })}
      </section>

      <div className="grid gap-6 lg:grid-cols-[3fr_2fr]">
        <Cartao
          titulo="Em andamento"
          semPadding
          acoes={
            <Link to="/ordens-servico" className="text-sm text-marca-600 hover:underline">
              Ver todas
            </Link>
          }
        >
          <EstadoConsulta consulta={ordens}>
            {() =>
              aguardandoAcao.length === 0 ? (
                <Vazio titulo="Nenhuma OS em andamento" />
              ) : (
                <ul className="divide-y divide-slate-100">
                  {aguardandoAcao.map((os) => (
                    <li key={os.id}>
                      <Link
                        to={`/ordens-servico/${os.id}`}
                        className="flex items-center justify-between gap-3 px-5 py-3 hover:bg-slate-50"
                      >
                        <div>
                          <p className="text-sm font-medium">
                            <span className="font-mono">{os.veiculo.placa}</span> · {os.cliente.nome}
                          </p>
                          <p className="font-mono text-xs text-slate-500">{codigoOS(os.id)}</p>
                        </div>
                        <SeloStatus status={os.status} />
                      </Link>
                    </li>
                  ))}
                </ul>
              )
            }
          </EstadoConsulta>
        </Cartao>

        <div className="space-y-6">
          <Cartao titulo="Tempo médio de execução">
            <EstadoConsulta consulta={tempoMedio}>
              {(t) =>
                t.ordensConsideradas === 0 ? (
                  <p className="text-sm text-slate-500">Ainda não há OS finalizadas para calcular.</p>
                ) : (
                  <>
                    <p className="text-3xl font-semibold">{formatarDuracao(t.mediaGeralMinutos)}</p>
                    <p className="text-xs text-slate-500">
                      da aprovação à finalização · {t.ordensConsideradas} OS
                    </p>
                    <ul className="mt-4 space-y-1.5 text-sm">
                      {t.porServico.map((s) => (
                        <li key={s.servicoId} className="flex justify-between gap-3">
                          <span className="text-slate-600">{s.servico}</span>
                          <span className="tabular-nums">{formatarDuracao(s.mediaMinutos)}</span>
                        </li>
                      ))}
                    </ul>
                  </>
                )
              }
            </EstadoConsulta>
          </Cartao>

          <Cartao
            titulo="Reposição de estoque"
            acoes={
              <Link to="/estoque?abaixoDoMinimo=true" className="text-sm text-marca-600 hover:underline">
                Abrir estoque
              </Link>
            }
          >
            <EstadoConsulta consulta={abaixoDoMinimo}>
              {(itens) =>
                itens.length === 0 ? (
                  <p className="text-sm text-slate-500">Todos os itens estão acima do mínimo.</p>
                ) : (
                  <ul className="space-y-2 text-sm">
                    {itens.map((p) => (
                      <li key={p.id} className="flex items-center justify-between gap-3">
                        <span>{p.nome}</span>
                        <Selo cor="vermelho">
                          {p.quantidadeEstoque} / mín. {p.estoqueMinimo}
                        </Selo>
                      </li>
                    ))}
                  </ul>
                )
              }
            </EstadoConsulta>
          </Cartao>
        </div>
      </div>
    </>
  )
}
