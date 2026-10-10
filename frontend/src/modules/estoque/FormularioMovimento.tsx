import { useState } from 'react'
import { mensagemDeErro } from '@/shared/api/http'
import { Alerta, Botao, Entrada, useNotificacoes } from '@/shared/ui'
import { useMovimentarEstoque, type Movimento, type Peca } from './api'

export function FormularioMovimento({
  peca,
  movimento,
  aoConcluir,
}: {
  peca: Peca
  movimento: Movimento
  aoConcluir: () => void
}) {
  const movimentar = useMovimentarEstoque()
  const notificar = useNotificacoes()
  const [quantidade, setQuantidade] = useState('')
  const valor = Number(quantidade)
  const valido = Number.isInteger(valor) && valor > 0
  const entrada = movimento === 'entrada'

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={(e) => {
        e.preventDefault()
        if (!valido) return
        movimentar.mutate(
          { id: peca.id, movimento, quantidade: valor },
          {
            onSuccess: (atualizada) => {
              notificar.sucesso(`Saldo de ${atualizada.nome}: ${atualizada.quantidadeEstoque}.`)
              aoConcluir()
            },
          },
        )
      }}
    >
      <p className="text-sm text-slate-600">
        <strong>{peca.nome}</strong> — saldo atual: <strong>{peca.quantidadeEstoque}</strong>
      </p>
      {movimentar.isError && <Alerta tipo="erro">{mensagemDeErro(movimentar.error)}</Alerta>}
      <Entrada
        rotulo={entrada ? 'Quantidade recebida' : 'Quantidade a baixar (ajuste/perda)'}
        type="number"
        min="1"
        autoFocus
        value={quantidade}
        onChange={(e) => setQuantidade(e.target.value)}
        erro={quantidade && !valido ? 'Informe um número inteiro maior que zero' : undefined}
      />
      <div className="flex justify-end gap-2 pt-2">
        <Botao variante="secundario" onClick={aoConcluir}>
          Cancelar
        </Botao>
        <Botao
          type="submit"
          variante={entrada ? 'primario' : 'perigo'}
          disabled={!valido}
          carregando={movimentar.isPending}
        >
          {entrada ? 'Registrar entrada' : 'Registrar saída'}
        </Botao>
      </div>
    </form>
  )
}
