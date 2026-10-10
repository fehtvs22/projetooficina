import { useMemo, useState } from 'react'
import { clientes } from '@/modules/clientes/api'
import { useFiltro } from '@/shared/hooks/useFiltro'
import { useRemocao } from '@/shared/hooks/useRemocao'
import { Botao, CabecalhoPagina, CampoBusca, Cartao, Dialogo, EstadoConsulta, Vazio } from '@/shared/ui'
import { veiculos, type Veiculo } from './api'
import { FormularioVeiculo } from './FormularioVeiculo'

export default function VeiculosPage() {
  const consulta = veiculos.useLista()
  const listaClientes = clientes.useLista()
  const remover = useRemocao(veiculos.useRemover())
  const [edicao, setEdicao] = useState<Veiculo | 'novo' | null>(null)

  const nomeDono = useMemo(
    () => new Map(listaClientes.data?.map((c) => [c.id, c.nome])),
    [listaClientes.data],
  )
  const { termo, setTermo, filtrados } = useFiltro(consulta.data, (v) => [
    v.placa,
    v.marca,
    v.modelo,
    nomeDono.get(v.clienteId),
  ])

  return (
    <>
      <CabecalhoPagina
        titulo="Veículos"
        descricao="Veículos dos clientes. Também podem ser cadastrados na abertura da OS."
        acoes={<Botao onClick={() => setEdicao('novo')}>Novo veículo</Botao>}
      />
      <Cartao
        semPadding
        titulo={
          <CampoBusca valor={termo} aoMudar={setTermo} placeholder="Buscar por placa, modelo ou dono" />
        }
      >
        <EstadoConsulta consulta={consulta}>
          {() =>
            filtrados.length === 0 ? (
              <Vazio titulo={termo ? 'Nenhum veículo encontrado' : 'Nenhum veículo cadastrado'} />
            ) : (
              <div className="overflow-x-auto">
                <table className="tabela">
                  <thead>
                    <tr>
                      <th>Placa</th>
                      <th>Veículo</th>
                      <th>Ano</th>
                      <th>Proprietário</th>
                      <th className="w-0" />
                    </tr>
                  </thead>
                  <tbody>
                    {filtrados.map((v) => (
                      <tr key={v.id}>
                        <td className="font-mono font-semibold">{v.placa}</td>
                        <td>
                          {v.marca} {v.modelo}
                        </td>
                        <td>{v.ano}</td>
                        <td className="text-slate-600">{nomeDono.get(v.clienteId) ?? '—'}</td>
                        <td>
                          <div className="flex gap-1">
                            <Botao variante="fantasma" tamanho="sm" onClick={() => setEdicao(v)}>
                              Editar
                            </Botao>
                            <Botao
                              variante="fantasma"
                              tamanho="sm"
                              onClick={() => remover(v.id, `Veículo ${v.placa}`)}
                            >
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
        titulo={edicao === 'novo' ? 'Novo veículo' : 'Editar veículo'}
      >
        <FormularioVeiculo
          key={edicao === 'novo' ? 'novo' : edicao?.id}
          veiculo={edicao === 'novo' || edicao === null ? undefined : edicao}
          aoConcluir={() => setEdicao(null)}
        />
      </Dialogo>
    </>
  )
}
