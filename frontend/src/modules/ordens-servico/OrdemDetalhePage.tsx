import { Link, useParams } from 'react-router'
import { pecas } from '@/modules/estoque/api'
import { servicos } from '@/modules/servicos/api'
import { mensagemDeErro } from '@/shared/api/http'
import { formatarDataHora, formatarDuracao } from '@/shared/dominio/formatos'
import { Cartao, EstadoConsulta, useNotificacoes } from '@/shared/ui'
import { ordensServicoApi, type TipoItem } from './api'
import { EditorDiagnostico } from './componentes/EditorDiagnostico'
import { LinhaDoTempo } from './componentes/LinhaDoTempo'
import { PainelAcoes } from './componentes/PainelAcoes'
import { ResumoOrcamento } from './componentes/ResumoOrcamento'
import { SeloStatus } from './componentes/SeloStatus'
import { SeletorItens } from './componentes/SeletorItens'
import { TabelaItens } from './componentes/TabelaItens'
import { codigoOS, permiteAlterarItens } from './dominio/status'
import { useOperacaoOS, useOrdem } from './hooks'
import type { OrdemServico } from './tipos'

export default function OrdemDetalhePage() {
  const { id = '' } = useParams()
  const consulta = useOrdem(id)
  return <EstadoConsulta consulta={consulta}>{(os) => <Detalhe os={os} />}</EstadoConsulta>
}

function Detalhe({ os }: { os: OrdemServico }) {
  const editavel = permiteAlterarItens(os.status)
  return (
    <>
      <div className="mb-6">
        <Link to="/ordens-servico" className="text-sm text-slate-500 hover:text-slate-800">
          ← Ordens de serviço
        </Link>
        <div className="mt-2 flex flex-wrap items-center gap-3">
          <h1 className="font-mono text-2xl font-semibold">OS {codigoOS(os.id)}</h1>
          <SeloStatus status={os.status} />
        </div>
        <p className="mt-1 text-sm text-slate-500">Aberta em {formatarDataHora(os.abertaEm)}</p>
      </div>

      <Cartao className="mb-6">
        <LinhaDoTempo os={os} />
      </Cartao>

      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <div className="space-y-6">
          <div className="grid gap-6 sm:grid-cols-2">
            <Cartao titulo="Cliente">
              <p className="font-medium">{os.cliente.nome}</p>
              <p className="font-mono text-sm text-slate-500">{os.cliente.documento}</p>
            </Cartao>
            <Cartao titulo="Veículo">
              <p className="font-mono font-semibold">{os.veiculo.placa}</p>
              <p className="text-sm text-slate-500">
                {os.veiculo.marca} {os.veiculo.modelo} · {os.veiculo.ano}
              </p>
            </Cartao>
          </div>

          <Cartao titulo="Problema relatado">
            <p className="text-sm whitespace-pre-line text-slate-700">
              {os.descricaoProblema || <span className="text-slate-400">Não informado.</span>}
            </p>
          </Cartao>

          <Cartao titulo="Diagnóstico">
            <EditorDiagnostico key={os.diagnostico ?? ''} os={os} />
          </Cartao>

          <ItensOS os={os} tipo="servicos" titulo="Serviços" editavel={editavel} />
          <ItensOS os={os} tipo="pecas" titulo="Peças e insumos" editavel={editavel} />
        </div>

        <aside className="space-y-6">
          <Cartao titulo="Próximo passo">
            <PainelAcoes os={os} />
          </Cartao>
          <Cartao titulo="Orçamento">
            <ResumoOrcamento orcamento={os.orcamento} />
          </Cartao>
          {(os.inicioExecucao || os.duracaoExecucaoMinutos !== undefined) && (
            <Cartao titulo="Execução">
              <dl className="space-y-1.5 text-sm">
                <div className="flex justify-between">
                  <dt className="text-slate-500">Início</dt>
                  <dd>{formatarDataHora(os.inicioExecucao)}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-slate-500">Fim</dt>
                  <dd>{formatarDataHora(os.fimExecucao)}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-slate-500">Duração</dt>
                  <dd>{formatarDuracao(os.duracaoExecucaoMinutos)}</dd>
                </div>
                {os.entregueEm && (
                  <div className="flex justify-between">
                    <dt className="text-slate-500">Entregue em</dt>
                    <dd>{formatarDataHora(os.entregueEm)}</dd>
                  </div>
                )}
              </dl>
            </Cartao>
          )}
        </aside>
      </div>
    </>
  )
}

function ItensOS({
  os,
  tipo,
  titulo,
  editavel,
}: {
  os: OrdemServico
  tipo: TipoItem
  titulo: string
  editavel: boolean
}) {
  const notificar = useNotificacoes()
  const catalogoServicos = servicos.useLista(undefined, { enabled: editavel && tipo === 'servicos' })
  const catalogoPecas = pecas.useLista(undefined, { enabled: editavel && tipo === 'pecas' })
  const adicionar = useOperacaoOS(os.id, (id, item: { id: string; quantidade: number }) =>
    ordensServicoApi.adicionarItem(id, tipo, item),
  )
  const remover = useOperacaoOS(os.id, (id, itemId: string) => ordensServicoApi.removerItem(id, tipo, itemId))
  const aoErrar = { onError: (erro: Error) => notificar.erro(mensagemDeErro(erro)) }

  const opcoes =
    tipo === 'servicos'
      ? (catalogoServicos.data ?? []).map((s) => ({ id: s.id, nome: s.nome, preco: s.preco }))
      : (catalogoPecas.data ?? []).map((p) => ({
          id: p.id,
          nome: p.nome,
          preco: p.preco,
          detalhe: `${p.quantidadeEstoque} em estoque`,
          indisponivel: p.quantidadeEstoque === 0,
        }))

  return (
    <Cartao titulo={titulo} semPadding>
      <TabelaItens
        itens={os[tipo]}
        vazio={tipo === 'servicos' ? 'Nenhum serviço incluído.' : 'Nenhuma peça ou insumo incluído.'}
        aoRemover={editavel ? (item) => remover.mutate(item.id, aoErrar) : undefined}
        removendo={remover.isPending ? remover.variables : undefined}
      />
      {editavel && (
        <div className="border-t border-slate-100 bg-slate-50/60 px-5 py-4">
          <SeletorItens
            rotulo={tipo === 'servicos' ? 'Incluir serviço' : 'Incluir peça ou insumo'}
            opcoes={opcoes}
            carregando={tipo === 'servicos' ? catalogoServicos.isPending : catalogoPecas.isPending}
            adicionando={adicionar.isPending}
            aoAdicionar={(id, quantidade) => adicionar.mutate({ id, quantidade }, aoErrar)}
          />
        </div>
      )}
    </Cartao>
  )
}
