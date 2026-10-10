import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { mensagemDeErro } from '@/shared/api/http'
import { aplicarErrosDaApi } from '@/shared/forms/errosApi'
import { Alerta, AreaTexto, Botao, Entrada, useNotificacoes } from '@/shared/ui'
import { servicos, type Servico } from './api'

const esquema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(120),
  descricao: z.string().trim().max(500),
  preco: z.number({ error: 'Informe o preço' }).min(0, 'O preço não pode ser negativo'),
  tempoEstimadoMinutos: z
    .number({ error: 'Informe o tempo estimado' })
    .int()
    .positive('Deve ser maior que zero'),
})
type Formulario = z.infer<typeof esquema>

export function FormularioServico({ servico, aoConcluir }: { servico?: Servico; aoConcluir: () => void }) {
  const salvar = servicos.useSalvar()
  const notificar = useNotificacoes()
  const { register, handleSubmit, setError, formState } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: servico ? { ...servico, descricao: servico.descricao ?? '' } : { nome: '', descricao: '' },
  })
  const erros = formState.errors

  const enviar = handleSubmit((dados) =>
    salvar.mutate(
      { id: servico?.id, dados: { ...dados, descricao: dados.descricao || undefined } },
      {
        onSuccess: () => {
          notificar.sucesso(servico ? 'Serviço atualizado.' : 'Serviço cadastrado.')
          aoConcluir()
        },
        onError: (erro) => aplicarErrosDaApi(erro, setError),
      },
    ),
  )

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      {salvar.isError && <Alerta tipo="erro">{mensagemDeErro(salvar.error)}</Alerta>}
      <Entrada rotulo="Nome" autoFocus erro={erros.nome?.message} {...register('nome')} />
      <AreaTexto rotulo="Descrição" erro={erros.descricao?.message} {...register('descricao')} />
      <div className="grid gap-4 sm:grid-cols-2">
        <Entrada
          rotulo="Preço (R$)"
          type="number"
          step="0.01"
          min="0"
          ajuda={servico ? 'OS já abertas mantêm o preço da inclusão.' : undefined}
          erro={erros.preco?.message}
          {...register('preco', { valueAsNumber: true })}
        />
        <Entrada
          rotulo="Tempo estimado (min)"
          type="number"
          min="1"
          erro={erros.tempoEstimadoMinutos?.message}
          {...register('tempoEstimadoMinutos', { valueAsNumber: true })}
        />
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
