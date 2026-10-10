import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { clientes } from '@/modules/clientes/api'
import { mensagemDeErro } from '@/shared/api/http'
import { normalizarPlaca, placaValida } from '@/shared/dominio/placa'
import { aplicarErrosDaApi } from '@/shared/forms/errosApi'
import { Alerta, Botao, Entrada, Selecao, useNotificacoes } from '@/shared/ui'
import { veiculos, type Veiculo } from './api'

const anoMaximo = new Date().getFullYear() + 1

const esquema = z.object({
  placa: z.string().refine(placaValida, 'Use o padrão ABC1234 ou Mercosul ABC1D23'),
  marca: z.string().trim().min(1, 'Informe a marca').max(60),
  modelo: z.string().trim().min(1, 'Informe o modelo').max(80),
  ano: z.number({ error: 'Informe o ano' }).int().min(1900, 'Ano inválido').max(anoMaximo, 'Ano inválido'),
  clienteId: z.string().min(1, 'Selecione o proprietário'),
})
type Formulario = z.infer<typeof esquema>

export function FormularioVeiculo({ veiculo, aoConcluir }: { veiculo?: Veiculo; aoConcluir: () => void }) {
  const salvar = veiculos.useSalvar()
  const listaClientes = clientes.useLista()
  const notificar = useNotificacoes()
  const { register, handleSubmit, setError, formState } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: veiculo ?? { placa: '', marca: '', modelo: '', clienteId: '' },
  })
  const erros = formState.errors

  const enviar = handleSubmit((dados) =>
    salvar.mutate(
      { id: veiculo?.id, dados: { ...dados, placa: normalizarPlaca(dados.placa) } },
      {
        onSuccess: () => {
          notificar.sucesso(veiculo ? 'Veículo atualizado.' : 'Veículo cadastrado.')
          aoConcluir()
        },
        onError: (erro) => aplicarErrosDaApi(erro, setError),
      },
    ),
  )

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      {salvar.isError && <Alerta tipo="erro">{mensagemDeErro(salvar.error)}</Alerta>}
      <Entrada
        rotulo="Placa"
        autoFocus={!veiculo}
        disabled={!!veiculo}
        className="uppercase"
        ajuda={veiculo ? 'A placa não pode ser alterada.' : undefined}
        erro={erros.placa?.message}
        {...register('placa')}
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <Entrada rotulo="Marca" erro={erros.marca?.message} {...register('marca')} />
        <Entrada rotulo="Modelo" erro={erros.modelo?.message} {...register('modelo')} />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Entrada
          rotulo="Ano"
          type="number"
          inputMode="numeric"
          erro={erros.ano?.message}
          {...register('ano', { valueAsNumber: true })}
        />
        <Selecao rotulo="Proprietário" erro={erros.clienteId?.message} {...register('clienteId')}>
          <option value="">{listaClientes.isPending ? 'Carregando…' : 'Selecione'}</option>
          {listaClientes.data?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome} — {c.documento}
            </option>
          ))}
        </Selecao>
      </div>
      <div className="flex justify-end gap-2 pt-2">
        <Botao variante="secundario" onClick={aoConcluir}>
          Cancelar
        </Botao>
        <Botao type="submit" carregando={salvar.isPending}>
          Salvar
        </Botao>
      </div>
    </form>
  )
}
