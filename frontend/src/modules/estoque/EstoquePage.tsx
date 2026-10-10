import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { formatarMoeda } from '@/shared/dominio/formatos'
import { useFiltro } from '@/shared/hooks/useFiltro'
import { useRemocao } from '@/shared/hooks/useRemocao'
import { Botao, CabecalhoPagina, CampoBusca, Cartao, Dialogo, EstadoConsulta, Selo, Vazio } from '@/shared/ui'
import { pecas, TIPOS_PECA, type Movimento, type Peca } from './api'
import { FormularioMovimento } from './FormularioMovimento'
import { FormularioPeca } from './FormularioPeca'

type Edicao = { tipo: 'cadastro'; peca?: Peca } | { tipo: 'movimento'; peca: Peca; movimento: Movimento }

export default function EstoquePage() {
  const [params, setParams] = useSearchParams()
  const abaixoDoMinimo = params.get('abaixoDoMinimo') === 'true'
  const consulta = pecas.useLista(abaixoDoMinimo ? { abaixoDoMinimo } : undefined)
  const remover = useRemocao(pecas.useRemover())
  const { termo, setTermo, filtrados } = useFiltro(consulta.data, (p) => [p.codigo, p.nome, p.descricao])
  const [edicao, setEdicao] = useState<Edicao | null>(null)

  const tituloDialogo =
    edicao?.tipo === 'movimento'
      ? edicao.movimento === 'entrada'
        ? 'Entrada no estoque'
        : 'Saída do estoque'
      : edicao?.peca
        ? 'Editar item'
        : 'Novo item'

  return (
    <>
      <CabecalhoPagina
        titulo="Peças e insumos"
        descricao="Cadastro e saldo de estoque. A aprovação do orçamento baixa o estoque automaticamente."
        acoes={<Botao onClick={() => setEdicao({ tipo: 'cadastro' })}>Novo item</Botao>}
      />
      <Cartao
        semPadding
        titulo={<CampoBusca valor={termo} aoMudar={setTermo} placeholder="Buscar por código ou nome" />}
        acoes={
          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              className="size-4 accent-marca-600"
              checked={abaixoDoMinimo}
              onChange={(e) =>
                setParams(e.target.checked ? { abaixoDoMinimo: 'true' } : {}, { replace: true })
              }
            />
            Só abaixo do mínimo
          </label>
        }
      >
        <EstadoConsulta consulta={consulta}>
          {() =>
            filtrados.length === 0 ? (
              <Vazio
                titulo={abaixoDoMinimo && !termo ? 'Nenhum item abaixo do mínimo' : 'Nenhum item encontrado'}
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="tabela">
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Item</th>
                      <th className="text-right">Preço</th>
                      <th className="text-right">Saldo</th>
                      <th className="text-right">Mínimo</th>
                      <th className="w-0" />
                    </tr>
                  </thead>
                  <tbody>
                    {filtrados.map((p) => (
                      <tr key={p.id}>
                        <td className="font-mono text-xs">{p.codigo}</td>
                        <td>
                          <div className="flex items-center gap-2 font-medium">
                            {p.nome}{' '}
                            <Selo cor={p.tipo === 'INSUMO' ? 'ciano' : 'cinza'}>{TIPOS_PECA[p.tipo]}</Selo>
                          </div>
                          {p.descricao && <div className="text-xs text-slate-500">{p.descricao}</div>}
                        </td>
                        <td className="text-right tabular-nums">{formatarMoeda(p.preco)}</td>
                        <td className="text-right tabular-nums">
                          {p.abaixoDoMinimo ? (
                            <Selo cor="vermelho">{p.quantidadeEstoque}</Selo>
                          ) : (
                            p.quantidadeEstoque
                          )}
                        </td>
                        <td className="text-right text-slate-500 tabular-nums">{p.estoqueMinimo}</td>
                        <td>
                          <div className="flex gap-1">
                            <Botao
                              variante="fantasma"
                              tamanho="sm"
                              onClick={() => setEdicao({ tipo: 'movimento', peca: p, movimento: 'entrada' })}
                            >
                              Entrada
                            </Botao>
                            <Botao
                              variante="fantasma"
                              tamanho="sm"
                              onClick={() => setEdicao({ tipo: 'movimento', peca: p, movimento: 'saida' })}
                            >
                              Saída
                            </Botao>
                            <Botao
                              variante="fantasma"
                              tamanho="sm"
                              onClick={() => setEdicao({ tipo: 'cadastro', peca: p })}
                            >
                              Editar
                            </Botao>
                            <Botao variante="fantasma" tamanho="sm" onClick={() => remover(p.id, p.nome)}>
                              Remover
                            </Botao>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          }
        </EstadoConsulta>
      </Cartao>
      <Dialogo aberto={edicao !== null} aoFechar={() => setEdicao(null)} titulo={tituloDialogo}>
        {edicao?.tipo === 'cadastro' && (
          <FormularioPeca
            key={edicao.peca?.id ?? 'novo'}
            peca={edicao.peca}
            aoConcluir={() => setEdicao(null)}
          />
        )}
        {edicao?.tipo === 'movimento' && (
          <FormularioMovimento
            key={edicao.peca.id + edicao.movimento}
            peca={edicao.peca}
            movimento={edicao.movimento}
            aoConcluir={() => setEdicao(null)}
          />
        )}
      </Dialogo>
    </>
  )
}
