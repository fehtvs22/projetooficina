import { useState } from 'react'
import { formatarDuracao, formatarMoeda } from '@/shared/dominio/formatos'
import { useFiltro } from '@/shared/hooks/useFiltro'
import { useRemocao } from '@/shared/hooks/useRemocao'
import { Botao, CabecalhoPagina, CampoBusca, Cartao, Dialogo, EstadoConsulta, Vazio } from '@/shared/ui'
import { servicos, type Servico } from './api'
import { FormularioServico } from './FormularioServico'

export default function ServicosPage() {
  const consulta = servicos.useLista()
  const remover = useRemocao(servicos.useRemover())
  const { termo, setTermo, filtrados } = useFiltro(consulta.data, (s) => [s.nome, s.descricao])
  const [edicao, setEdicao] = useState<Servico | 'novo' | null>(null)

  return (
    <>
      <CabecalhoPagina
        titulo="Serviços"
        descricao="Catálogo de mão de obra usado nos orçamentos."
        acoes={<Botao onClick={() => setEdicao('novo')}>Novo serviço</Botao>}
      />
      <Cartao
        semPadding
        titulo={<CampoBusca valor={termo} aoMudar={setTermo} placeholder="Buscar serviço" />}
      >
        <EstadoConsulta consulta={consulta}>
          {() =>
            filtrados.length === 0 ? (
              <Vazio titulo={termo ? 'Nenhum serviço encontrado' : 'Nenhum serviço cadastrado'} />
            ) : (
              <div className="overflow-x-auto">
                <table className="tabela">
                  <thead>
                    <tr>
                      <th>Serviço</th>
                      <th className="text-right">Preço</th>
                      <th className="text-right">Tempo estimado</th>
                      <th className="w-0" />
                    </tr>
                  </thead>
                  <tbody>
                    {filtrados.map((s) => (
                      <tr key={s.id}>
                        <td>
                          <div className="font-medium">{s.nome}</div>
                          {s.descricao && <div className="text-xs text-slate-500">{s.descricao}</div>}
                        </td>
                        <td className="text-right tabular-nums">{formatarMoeda(s.preco)}</td>
                        <td className="text-right">{formatarDuracao(s.tempoEstimadoMinutos)}</td>
                        <td>
                          <div className="flex gap-1">
                            <Botao variante="fantasma" tamanho="sm" onClick={() => setEdicao(s)}>
                              Editar
                            </Botao>
                            <Botao variante="fantasma" tamanho="sm" onClick={() => remover(s.id, s.nome)}>
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
      <Dialogo
        aberto={edicao !== null}
        aoFechar={() => setEdicao(null)}
        titulo={edicao === 'novo' ? 'Novo serviço' : 'Editar serviço'}
      >
        <FormularioServico
          key={edicao === 'novo' ? 'novo' : edicao?.id}
          servico={edicao === 'novo' || edicao === null ? undefined : edicao}
          aoConcluir={() => setEdicao(null)}
        />
      </Dialogo>
    </>
  )
}
