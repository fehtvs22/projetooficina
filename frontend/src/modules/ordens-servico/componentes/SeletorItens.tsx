import { useState } from 'react'
import { formatarMoeda } from '@/shared/dominio/formatos'
import { Botao } from '@/shared/ui'

export type OpcaoItem = { id: string; nome: string; preco: number; detalhe?: string; indisponivel?: boolean }

type Props = {
  rotulo: string
  opcoes: OpcaoItem[]
  carregando?: boolean
  adicionando?: boolean
  aoAdicionar: (id: string, quantidade: number) => void
}

/** Escolha de um item do catálogo + quantidade. */
export function SeletorItens({ rotulo, opcoes, carregando, adicionando, aoAdicionar }: Props) {
  const [id, setId] = useState('')
  const [quantidade, setQuantidade] = useState(1)
  const valido = id !== '' && Number.isInteger(quantidade) && quantidade > 0

  return (
    <div className="flex flex-wrap items-end gap-2">
      <label className="min-w-56 flex-1 text-sm">
        <span className="mb-1 block font-medium text-slate-700">{rotulo}</span>
        <select
          value={id}
          onChange={(e) => setId(e.target.value)}
          className="h-9 w-full rounded-lg border border-slate-300 bg-white px-2 text-sm"
        >
          <option value="">{carregando ? 'Carregando…' : 'Selecione'}</option>
          {opcoes.map((o) => (
            <option key={o.id} value={o.id} disabled={o.indisponivel}>
              {o.nome} — {formatarMoeda(o.preco)}
              {o.detalhe ? ` (${o.detalhe})` : ''}
            </option>
          ))}
        </select>
      </label>
      <label className="w-20 text-sm">
        <span className="mb-1 block font-medium text-slate-700">Qtd.</span>
        <input
          type="number"
          min={1}
          value={quantidade}
          onChange={(e) => setQuantidade(e.target.valueAsNumber)}
          className="h-9 w-full rounded-lg border border-slate-300 bg-white px-2 text-sm"
        />
      </label>
      <Botao
        variante="secundario"
        tamanho="sm"
        className="h-9"
        disabled={!valido}
        carregando={adicionando}
        onClick={() => {
          aoAdicionar(id, quantidade)
          setId('')
          setQuantidade(1)
        }}
      >
        Adicionar
      </Botao>
    </div>
  )
}
