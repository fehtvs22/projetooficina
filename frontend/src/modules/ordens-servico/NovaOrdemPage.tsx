import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useFieldArray, useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import { clientes } from '@/modules/clientes/api'
import { pecas } from '@/modules/estoque/api'
import { servicos } from '@/modules/servicos/api'
import { veiculos } from '@/modules/veiculos/api'
import { ApiError, mensagemDeErro } from '@/shared/api/http'
import { documentoValido, mascararDocumento, normalizarDocumento } from '@/shared/dominio/documento'
import { formatarMoeda } from '@/shared/dominio/formatos'
import { normalizarPlaca, placaValida } from '@/shared/dominio/placa'
import { aplicarErrosDaApi } from '@/shared/forms/errosApi'
import {
  Alerta,
  AreaTexto,
  Botao,
  CabecalhoPagina,
  Cartao,
  Entrada,
  Spinner,
  useNotificacoes,
} from '@/shared/ui'
import { SeletorItens } from './componentes/SeletorItens'
import { useAbrirOrdem } from './hooks'

const item = z.object({ itemId: z.string(), quantidade: z.number().int().positive() })

const esquema = z.object({
  documentoCliente: z.string().refine(documentoValido, 'CPF ou CNPJ inválido'),
  veiculo: z.object({
    placa: z.string().refine(placaValida, 'Use o padrão ABC1234 ou Mercosul ABC1D23'),
    marca: z.string().trim().max(60),
    modelo: z.string().trim().max(80),
    ano: z
      .number()
      .int()
      .min(1900, 'Ano inválido')
      .max(new Date().getFullYear() + 1, 'Ano inválido')
      .optional(),
  }),
  descricaoProblema: z.string().trim().max(1000),
  servicos: z.array(item),
  pecas: z.array(item),
})
type Formulario = z.infer<typeof esquema>

/** Busca o cliente pelo documento assim que ele fica válido. 404 = cliente não cadastrado. */
function useClientePorDocumento(documento: string) {
  const valido = documentoValido(documento)
  return useQuery({
    queryKey: ['clientes', 'documento', normalizarDocumento(documento)],
    queryFn: async () => {
      try {
        const [cliente] = await clientes.api.listar({ documento })
        return cliente ?? null
      } catch (erro) {
        if (erro instanceof ApiError && erro.status === 404) return null
        throw erro
      }
    },
    enabled: valido,
  })
}

export default function NovaOrdemPage() {
  const navegar = useNavigate()
  const notificar = useNotificacoes()
  const abrir = useAbrirOrdem()
  const catalogoServicos = servicos.useLista()
  const catalogoPecas = pecas.useLista()

  const { register, handleSubmit, control, setValue, setError, formState } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: {
      documentoCliente: '',
      veiculo: { placa: '', marca: '', modelo: '' },
      descricaoProblema: '',
      servicos: [],
      pecas: [],
    },
  })
  const erros = formState.errors
  const listaServicos = useFieldArray({ control, name: 'servicos' })
  const listaPecas = useFieldArray({ control, name: 'pecas' })
  const [documento, placa, servicosEscolhidos, pecasEscolhidas] = useWatch({
    control,
    name: ['documentoCliente', 'veiculo.placa', 'servicos', 'pecas'],
  })

  const cliente = useClientePorDocumento(documento)
  const veiculosDoCliente = veiculos.useLista(cliente.data ? { clienteId: cliente.data.id } : undefined, {
    enabled: !!cliente.data,
  })
  const veiculoExistente = veiculosDoCliente.data?.find((v) => v.placa === normalizarPlaca(placa))

  const precoServico = new Map(catalogoServicos.data?.map((s) => [s.id, s]))
  const precoPeca = new Map(catalogoPecas.data?.map((p) => [p.id, p]))
  const estimativa =
    servicosEscolhidos.reduce((t, i) => t + (precoServico.get(i.itemId)?.preco ?? 0) * i.quantidade, 0) +
    pecasEscolhidas.reduce((t, i) => t + (precoPeca.get(i.itemId)?.preco ?? 0) * i.quantidade, 0)

  const enviar = handleSubmit((dados) => {
    if (!cliente.data) {
      setError('documentoCliente', { message: 'Cliente não cadastrado' })
      return
    }
    if (!veiculoExistente && (!dados.veiculo.marca || !dados.veiculo.modelo || !dados.veiculo.ano)) {
      if (!dados.veiculo.marca) setError('veiculo.marca', { message: 'Informe a marca do veículo novo' })
      if (!dados.veiculo.modelo) setError('veiculo.modelo', { message: 'Informe o modelo do veículo novo' })
      if (!dados.veiculo.ano) setError('veiculo.ano', { message: 'Informe o ano do veículo novo' })
      return
    }
    abrir.mutate(
      {
        documentoCliente: dados.documentoCliente,
        veiculo: veiculoExistente
          ? { placa: veiculoExistente.placa }
          : { ...dados.veiculo, placa: normalizarPlaca(dados.veiculo.placa) },
        descricaoProblema: dados.descricaoProblema || undefined,
        servicos: dados.servicos.map(({ itemId, quantidade }) => ({ id: itemId, quantidade })),
        pecas: dados.pecas.map(({ itemId, quantidade }) => ({ id: itemId, quantidade })),
      },
      {
        onSuccess: (os) => {
          notificar.sucesso('Ordem de serviço aberta.')
          navegar(`/ordens-servico/${os.id}`)
        },
        onError: (erro) => aplicarErrosDaApi(erro, setError),
      },
    )
  })

  return (
    <form onSubmit={enviar} noValidate>
      <CabecalhoPagina
        titulo="Abrir ordem de serviço"
        descricao="A OS começa no status Recebida. Serviços e peças também podem ser incluídos depois, no diagnóstico."
      />
      {abrir.isError && (
        <div className="mb-4">
          <Alerta tipo="erro" titulo="Não foi possível abrir a OS">
            {mensagemDeErro(abrir.error)}
          </Alerta>
        </div>
      )}
      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <div className="space-y-6">
          <Cartao titulo="1. Cliente">
            <Entrada
              rotulo="CPF ou CNPJ do cliente"
              autoFocus
              erro={erros.documentoCliente?.message}
              {...register('documentoCliente', {
                onChange: (e) => setValue('documentoCliente', mascararDocumento(e.target.value)),
              })}
            />
            <div className="mt-3 text-sm" aria-live="polite">
              {cliente.isFetching && (
                <span className="flex items-center gap-2 text-slate-500">
                  <Spinner className="size-4" /> Buscando cliente…
                </span>
              )}
              {cliente.data && (
                <p className="rounded-lg bg-emerald-50 px-3 py-2 text-emerald-800">
                  Cliente: <strong>{cliente.data.nome}</strong>
                </p>
              )}
              {cliente.data === null && (
                <Alerta tipo="aviso">
                  Nenhum cliente com este documento.{' '}
                  <Link to="/clientes" className="font-medium underline">
                    Cadastre o cliente
                  </Link>{' '}
                  antes de abrir a OS.
                </Alerta>
              )}
            </div>
          </Cartao>

          <Cartao titulo="2. Veículo">
            {!!veiculosDoCliente.data?.length && (
              <div className="mb-4 flex flex-wrap gap-2">
                <span className="w-full text-xs font-medium text-slate-500">Veículos do cliente</span>
                {veiculosDoCliente.data.map((v) => (
                  <button
                    key={v.id}
                    type="button"
                    onClick={() => setValue('veiculo.placa', v.placa, { shouldValidate: true })}
                    aria-pressed={veiculoExistente?.id === v.id}
                    className="rounded-lg border border-slate-200 px-3 py-1.5 text-left text-sm hover:border-marca-600 aria-pressed:border-marca-600 aria-pressed:bg-marca-50"
                  >
                    <span className="font-mono font-semibold">{v.placa}</span>{' '}
                    <span className="text-slate-500">
                      {v.marca} {v.modelo}
                    </span>
                  </button>
                ))}
              </div>
            )}
            <Entrada
              rotulo="Placa"
              className="uppercase"
              erro={erros.veiculo?.placa?.message}
              {...register('veiculo.placa')}
            />
            {placaValida(placa) && !veiculoExistente && (
              <div className="mt-4 space-y-3">
                <p className="text-sm text-slate-500">Veículo novo: será cadastrado para o cliente.</p>
                <div className="grid gap-4 sm:grid-cols-3">
                  <Entrada
                    rotulo="Marca"
                    erro={erros.veiculo?.marca?.message}
                    {...register('veiculo.marca')}
                  />
                  <Entrada
                    rotulo="Modelo"
                    erro={erros.veiculo?.modelo?.message}
                    {...register('veiculo.modelo')}
                  />
                  <Entrada
                    rotulo="Ano"
                    type="number"
                    erro={erros.veiculo?.ano?.message}
                    {...register('veiculo.ano', { setValueAs: (v) => (v === '' ? undefined : Number(v)) })}
                  />
                </div>
              </div>
            )}
          </Cartao>

          <Cartao titulo="3. Problema relatado">
            <AreaTexto
              rotulo="Descrição do problema"
              placeholder="Ex.: barulho ao frear, luz da injeção acesa…"
              erro={erros.descricaoProblema?.message}
              {...register('descricaoProblema')}
            />
          </Cartao>

          <Cartao titulo="4. Serviços e peças (opcional)">
            <div className="space-y-5">
              <SeletorItens
                rotulo="Serviço"
                carregando={catalogoServicos.isPending}
                opcoes={(catalogoServicos.data ?? []).map((s) => ({
                  id: s.id,
                  nome: s.nome,
                  preco: s.preco,
                }))}
                aoAdicionar={(id, quantidade) => listaServicos.append({ itemId: id, quantidade })}
              />
              <SeletorItens
                rotulo="Peça ou insumo"
                carregando={catalogoPecas.isPending}
                opcoes={(catalogoPecas.data ?? []).map((p) => ({
                  id: p.id,
                  nome: p.nome,
                  preco: p.preco,
                  detalhe: `${p.quantidadeEstoque} em estoque`,
                  indisponivel: p.quantidadeEstoque === 0,
                }))}
                aoAdicionar={(id, quantidade) => listaPecas.append({ itemId: id, quantidade })}
              />
              {(listaServicos.fields.length > 0 || listaPecas.fields.length > 0) && (
                <ul className="divide-y divide-slate-100 rounded-lg border border-slate-200 text-sm">
                  {listaServicos.fields.map((f, i) => (
                    <li key={f.id} className="flex items-center justify-between gap-3 px-3 py-2">
                      <span>
                        {f.quantidade}× {precoServico.get(f.itemId)?.nome}
                      </span>
                      <Botao variante="fantasma" tamanho="sm" onClick={() => listaServicos.remove(i)}>
                        Remover
                      </Botao>
                    </li>
                  ))}
                  {listaPecas.fields.map((f, i) => (
                    <li key={f.id} className="flex items-center justify-between gap-3 px-3 py-2">
                      <span>
                        {f.quantidade}× {precoPeca.get(f.itemId)?.nome}
                      </span>
                      <Botao variante="fantasma" tamanho="sm" onClick={() => listaPecas.remove(i)}>
                        Remover
                      </Botao>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </Cartao>
        </div>

        <aside className="lg:sticky lg:top-6 lg:self-start">
          <Cartao titulo="Resumo">
            <dl className="space-y-2 text-sm">
              <div className="flex justify-between">
                <dt className="text-slate-500">Cliente</dt>
                <dd className="text-right font-medium">{cliente.data?.nome ?? '—'}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-slate-500">Veículo</dt>
                <dd className="font-mono font-medium">{placaValida(placa) ? normalizarPlaca(placa) : '—'}</dd>
              </div>
              <div className="flex justify-between border-t border-slate-100 pt-2 text-base font-semibold">
                <dt>Estimativa</dt>
                <dd className="tabular-nums">{formatarMoeda(estimativa)}</dd>
              </div>
            </dl>
            <p className="mt-2 text-xs text-slate-500">
              O valor final é calculado pelo servidor com os preços vigentes.
            </p>
            <Botao
              type="submit"
              className="mt-4 w-full"
              carregando={abrir.isPending}
              disabled={!cliente.data}
            >
              Abrir ordem de serviço
            </Botao>
          </Cartao>
        </aside>
      </div>
    </form>
  )
}
