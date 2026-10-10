import { useState } from 'react'
import { mensagemDeErro } from '@/shared/api/http'
import { Alerta, AreaTexto, Botao, useNotificacoes } from '@/shared/ui'
import { ordensServicoApi } from '../api'
import { useOperacaoOS } from '../hooks'
import type { OrdemServico } from '../tipos'

export function EditorDiagnostico({ os }: { os: OrdemServico }) {
  const editavel = os.status === 'EM_DIAGNOSTICO'
  const [texto, setTexto] = useState(os.diagnostico ?? '')
  const notificar = useNotificacoes()
  const salvar = useOperacaoOS(os.id, (id, diagnostico: string) =>
    ordensServicoApi.registrarDiagnostico(id, diagnostico),
  )

  if (!editavel) {
    return (
      <p className="text-sm whitespace-pre-line text-slate-700">
        {os.diagnostico || <span className="text-slate-400">Sem laudo registrado.</span>}
      </p>
    )
  }

  const alterado = texto.trim() !== (os.diagnostico ?? '')
  return (
    <form
      className="space-y-3"
      onSubmit={(e) => {
        e.preventDefault()
        salvar.mutate(texto.trim(), { onSuccess: () => notificar.sucesso('Diagnóstico registrado.') })
      }}
    >
      {salvar.isError && <Alerta tipo="erro">{mensagemDeErro(salvar.error)}</Alerta>}
      <AreaTexto
        rotulo="Laudo do diagnóstico"
        rows={4}
        maxLength={2000}
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
      />
      <div className="flex justify-end">
        <Botao
          type="submit"
          tamanho="sm"
          variante="secundario"
          disabled={!texto.trim() || !alterado}
          carregando={salvar.isPending}
        >
          Salvar diagnóstico
        </Botao>
      </div>
    </form>
  )
}
