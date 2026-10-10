import { useState } from 'react'
import { mensagemDeErro } from '@/shared/api/http'
import { AreaTexto, Botao, Dialogo, useConfirmacao, useNotificacoes } from '@/shared/ui'
import { ordensServicoApi } from '../api'
import { acoesDaOficina, type AcaoOS, type ComandoOS } from '../dominio/status'
import { useOperacaoOS } from '../hooks'
import type { OrdemServico } from '../tipos'

const CONFIRMACOES: Partial<Record<ComandoOS, string>> = {
  enviarOrcamento:
    'Depois do envio, os itens não podem mais ser alterados. O cliente poderá aprovar ou rejeitar o orçamento.',
  aprovarOrcamento: 'A OS entra em execução e as peças são baixadas do estoque.',
  finalizar: 'Confirma que todos os serviços foram executados?',
  entregar: 'Confirma a entrega do veículo ao cliente?',
}

export function PainelAcoes({ os }: { os: OrdemServico }) {
  const acoes = acoesDaOficina(os)
  const confirmar = useConfirmacao()
  const notificar = useNotificacoes()
  const executar = useOperacaoOS(os.id, (id, { comando, motivo }: { comando: ComandoOS; motivo?: string }) =>
    ordensServicoApi.executar(id, comando, motivo),
  )
  const [rejeitando, setRejeitando] = useState(false)
  const [motivo, setMotivo] = useState('')

  const disparar = (comando: ComandoOS, motivoRejeicao?: string) =>
    executar.mutate(
      { comando, motivo: motivoRejeicao },
      {
        onSuccess: () => {
          setRejeitando(false)
          notificar.sucesso('Ordem de serviço atualizada.')
        },
        onError: (erro) => notificar.erro(mensagemDeErro(erro)),
      },
    )

  const aoClicar = async (acao: AcaoOS) => {
    if (acao.comando === 'rejeitarOrcamento') {
      setMotivo('')
      setRejeitando(true)
      return
    }
    const mensagem = CONFIRMACOES[acao.comando]
    if (mensagem && !(await confirmar({ titulo: acao.rotulo, mensagem, confirmar: acao.rotulo }))) return
    disparar(acao.comando)
  }

  if (acoes.length === 0)
    return <p className="text-sm text-slate-500">Ciclo concluído: o veículo foi entregue.</p>

  return (
    <div className="space-y-2">
      {acoes.map((acao) => (
        <div key={acao.comando}>
          <Botao
            className="w-full"
            variante={acao.destrutiva ? 'secundario' : 'primario'}
            disabled={!!acao.bloqueio}
            carregando={executar.isPending && executar.variables?.comando === acao.comando}
            onClick={() => void aoClicar(acao)}
          >
            {acao.rotulo}
          </Botao>
          {acao.bloqueio && <p className="mt-1 text-xs text-slate-500">{acao.bloqueio}</p>}
        </div>
      ))}
      <Dialogo
        aberto={rejeitando}
        aoFechar={() => setRejeitando(false)}
        titulo="Registrar rejeição do orçamento"
        rodape={
          <>
            <Botao variante="secundario" onClick={() => setRejeitando(false)}>
              Cancelar
            </Botao>
            <Botao
              variante="perigo"
              carregando={executar.isPending}
              onClick={() => disparar('rejeitarOrcamento', motivo.trim())}
            >
              Rejeitar orçamento
            </Botao>
          </>
        }
      >
        <p className="mb-3 text-sm text-slate-600">
          A OS será finalizada sem execução e o veículo aguardará retirada.
        </p>
        <AreaTexto
          rotulo="Motivo (opcional)"
          maxLength={500}
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
        />
      </Dialogo>
    </div>
  )
}
