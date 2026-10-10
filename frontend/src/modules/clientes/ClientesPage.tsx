import { useState } from 'react'
import { useFiltro } from '@/shared/hooks/useFiltro'
import { useRemocao } from '@/shared/hooks/useRemocao'
import { Botao, CabecalhoPagina, CampoBusca, Cartao, Dialogo, EstadoConsulta, Selo, Vazio } from '@/shared/ui'
import { clientes, type Cliente } from './api'
import { FormularioCliente } from './FormularioCliente'

export default function ClientesPage() {
  const consulta = clientes.useLista()
  const remover = useRemocao(clientes.useRemover())
  const { termo, setTermo, filtrados } = useFiltro(consulta.data, (c) => [c.nome, c.documento, c.email])
  const [edicao, setEdicao] = useState<Cliente | 'novo' | null>(null)

  return (
    <>
      <CabecalhoPagina
        titulo="Clientes"
        descricao="Pessoas físicas e jurídicas atendidas pela oficina."
        acoes={<Botao onClick={() => setEdicao('novo')}>Novo cliente</Botao>}
      />
      <Cartao
        semPadding
        titulo={
          <CampoBusca valor={termo} aoMudar={setTermo} placeholder="Buscar por nome, documento ou e-mail" />
        }
      >
        <EstadoConsulta consulta={consulta}>
          {() =>
            filtrados.length === 0 ? (
              <Vazio titulo={termo ? 'Nenhum cliente encontrado' : 'Nenhum cliente cadastrado'} />
            ) : (
              <div className="overflow-x-auto">
                <table className="tabela">
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>Documento</th>
                      <th>Contato</th>
                      <th className="w-0" />
                    </tr>
                  </thead>
                  <tbody>
                    {filtrados.map((c) => (
                      <tr key={c.id}>
                        <td className="font-medium">{c.nome}</td>
                        <td>
                          <span className="font-mono text-xs">{c.documento}</span>{' '}
                          <Selo>{c.tipoDocumento}</Selo>
                        </td>
                        <td className="text-slate-600">
                          {c.email && <div>{c.email}</div>}
                          {c.telefone && <div>{c.telefone}</div>}
                          {!c.email && !c.telefone && '—'}
                        </td>
                        <td>
                          <div className="flex gap-1">
                            <Botao variante="fantasma" tamanho="sm" onClick={() => setEdicao(c)}>
                              Editar
                            </Botao>
                            <Botao variante="fantasma" tamanho="sm" onClick={() => remover(c.id, c.nome)}>
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
        titulo={edicao === 'novo' ? 'Novo cliente' : 'Editar cliente'}
      >
        <FormularioCliente
          key={edicao === 'novo' ? 'novo' : edicao?.id}
          cliente={edicao === 'novo' || edicao === null ? undefined : edicao}
          aoConcluir={() => setEdicao(null)}
        />
      </Dialogo>
    </>
  )
}
