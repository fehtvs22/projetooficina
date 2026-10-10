import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { LinhaDoTempo } from '@/modules/ordens-servico/componentes/LinhaDoTempo'
import { ResumoOrcamento } from '@/modules/ordens-servico/componentes/ResumoOrcamento'
import { codigoOS } from '@/modules/ordens-servico/dominio/status'
import { SeloStatus } from '@/modules/ordens-servico/componentes/SeloStatus'
import { TabelaItens } from '@/modules/ordens-servico/componentes/TabelaItens'
import type { OrdemServico } from '@/modules/ordens-servico/tipos'
import { mensagemDeErro } from '@/shared/api/http'
import { documentoValido, mascararDocumento, normalizarDocumento } from '@/shared/dominio/documento'
import { formatarDataHora } from '@/shared/dominio/formatos'
import { Alerta, AreaTexto, Botao, Cartao, Carregando, Entrada, Vazio } from '@/shared/ui'
import { acompanhamentoApi } from './api'

/**
 * O documento fica só em memória (não vai para URL nem para o storage): CPF é dado pessoal (LGPD).
 */
export default function AcompanhamentoPage() {
  const [digitado, setDigitado] = useState('')
  const [documento, setDocumento] = useState<string | null>(null)
  const [tocado, setTocado] = useState(false)

  const consulta = useQuery({
    queryKey: ['acompanhamento', documento ? normalizarDocumento(documento) : ''],
    queryFn: () => acompanhamentoApi.listar(documento!),
    enabled: !!documento,
  })

  const invalido = tocado && !documentoValido(digitado)

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-2xl font-semibold tracking-tight">Acompanhe seu veículo</h1>
      <p className="mt-1 text-sm text-slate-500">
        Informe o CPF ou CNPJ usado na abertura da ordem de serviço.
      </p>

      <form
        noValidate
        className="mt-6 flex flex-wrap items-start gap-3"
        onSubmit={(e) => {
          e.preventDefault()
          setTocado(true)
          if (documentoValido(digitado)) setDocumento(digitado)
        }}
      >
        <Entrada
          rotulo="CPF ou CNPJ"
          className="min-w-64 flex-1"
          value={digitado}
          autoComplete="off"
          onChange={(e) => setDigitado(mascararDocumento(e.target.value))}
          erro={invalido ? 'CPF ou CNPJ inválido' : undefined}
        />
        <Botao type="submit" className="mt-6.5" carregando={consulta.isFetching}>
          Consultar
        </Botao>
      </form>

      <div className="mt-8 space-y-6">
        {consulta.isPending && documento && <Carregando />}
        {consulta.isError && <Alerta tipo="erro">{mensagemDeErro(consulta.error)}</Alerta>}
        {consulta.data?.length === 0 && (
          <Cartao>
            <Vazio
              titulo="Nenhuma ordem de serviço encontrada"
              descricao="Confira o documento ou fale com a oficina."
            />
          </Cartao>
        )}
        {documento &&
          consulta.data
            ?.slice()
            .reverse()
            .map((os) => <OrdemDoCliente key={os.id} os={os} documento={documento} />)}
      </div>
    </div>
  )
}

function OrdemDoCliente({ os, documento }: { os: OrdemServico; documento: string }) {
  const cliente = useQueryClient()
  const [rejeitando, setRejeitando] = useState(false)
  const [motivo, setMotivo] = useState('')
  const responder = useMutation({
    mutationFn: ({ aprovado, motivo }: { aprovado: boolean; motivo?: string }) =>
      acompanhamentoApi.responder(os.id, documento, aprovado, motivo),
    onSuccess: () => cliente.invalidateQueries({ queryKey: ['acompanhamento'] }),
  })
  const aguardando = os.status === 'AGUARDANDO_APROVACAO'

  return (
    <Cartao
      titulo={
        <span className="flex items-center gap-3">
          <span className="font-mono">OS {codigoOS(os.id)}</span>
          <SeloStatus status={os.status} />
        </span>
      }
      acoes={<span className="text-xs text-slate-500">Aberta em {formatarDataHora(os.abertaEm)}</span>}
    >
      <p className="mb-5 text-sm">
        <span className="font-mono font-semibold">{os.veiculo.placa}</span> · {os.veiculo.marca}{' '}
        {os.veiculo.modelo}
      </p>
      <LinhaDoTempo os={os} />

      {os.diagnostico && (
        <div className="mt-6">
          <h3 className="text-sm font-semibold">Diagnóstico</h3>
          <p className="mt-1 text-sm whitespace-pre-line text-slate-700">{os.diagnostico}</p>
        </div>
      )}

      {os.orcamento.situacao !== 'EM_ELABORACAO' && (
        <div className="mt-6 grid gap-6 md:grid-cols-[3fr_2fr]">
          <div className="overflow-hidden rounded-lg border border-slate-200">
            <TabelaItens itens={[...os.servicos, ...os.pecas]} vazio="Sem itens." />
          </div>
          <ResumoOrcamento orcamento={os.orcamento} />
        </div>
      )}

      {aguardando && (
        <div className="mt-6 rounded-lg border border-amber-200 bg-amber-50 p-4">
          <p className="text-sm font-medium text-amber-900">O orçamento aguarda sua resposta.</p>
          {responder.isError && (
            <div className="mt-3">
              <Alerta tipo="erro">{mensagemDeErro(responder.error)}</Alerta>
            </div>
          )}
          {rejeitando ? (
            <div className="mt-3 space-y-3">
              <AreaTexto
                rotulo="Conte o motivo (opcional)"
                maxLength={500}
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
              />
              <div className="flex gap-2">
                <Botao
                  variante="perigo"
                  carregando={responder.isPending}
                  onClick={() => responder.mutate({ aprovado: false, motivo: motivo.trim() })}
                >
                  Confirmar rejeição
                </Botao>
                <Botao variante="secundario" onClick={() => setRejeitando(false)}>
                  Voltar
                </Botao>
              </div>
            </div>
          ) : (
            <div className="mt-3 flex flex-wrap gap-2">
              <Botao
                variante="sucesso"
                carregando={responder.isPending}
                onClick={() => responder.mutate({ aprovado: true })}
              >
                Aprovar orçamento
              </Botao>
              <Botao variante="secundario" disabled={responder.isPending} onClick={() => setRejeitando(true)}>
                Rejeitar
              </Botao>
            </div>
          )}
        </div>
      )}
    </Cartao>
  )
}
