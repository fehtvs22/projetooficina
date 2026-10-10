import { formatarMoeda } from '@/shared/dominio/formatos'
import { Botao } from '@/shared/ui'
import type { ItemOS } from '../tipos'

type Props = { itens: ItemOS[]; vazio: string; aoRemover?: (item: ItemOS) => void; removendo?: string }

export function TabelaItens({ itens, vazio, aoRemover, removendo }: Props) {
  if (itens.length === 0) return <p className="px-5 py-6 text-center text-sm text-slate-500">{vazio}</p>
  return (
    <div className="overflow-x-auto">
      <table className="tabela">
        <thead>
          <tr>
            <th>Descrição</th>
            <th className="text-right">Qtd.</th>
            <th className="text-right">Unitário</th>
            <th className="text-right">Subtotal</th>
            {aoRemover && <th className="w-0" />}
          </tr>
        </thead>
        <tbody>
          {itens.map((item) => (
            <tr key={item.id}>
              <td>{item.descricao}</td>
              <td className="text-right tabular-nums">{item.quantidade}</td>
              <td className="text-right tabular-nums">{formatarMoeda(item.precoUnitario)}</td>
              <td className="text-right font-medium tabular-nums">{formatarMoeda(item.subtotal)}</td>
              {aoRemover && (
                <td>
                  <Botao
                    variante="fantasma"
                    tamanho="sm"
                    carregando={removendo === item.id}
                    onClick={() => aoRemover(item)}
                    aria-label={`Remover ${item.descricao}`}
                  >
                    Remover
                  </Botao>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
