import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { mensagemDeErro } from '@/shared/api/http'
import { documentoValido, mascararDocumento } from '@/shared/dominio/documento'
import { aplicarErrosDaApi } from '@/shared/forms/errosApi'
import { Alerta, Botao, Entrada, useNotificacoes } from '@/shared/ui'
import { clientes, type Cliente } from './api'

const esquema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(150),
  documento: z.string().refine(documentoValido, 'CPF ou CNPJ inválido'),
  email: z.union([z.literal(''), z.email('E-mail inválido').max(150)]),
  telefone: z.string().trim().max(20),
})
type Formulario = z.infer<typeof esquema>

export function FormularioCliente({ cliente, aoConcluir }: { cliente?: Cliente; aoConcluir: () => void }) {
  const salvar = clientes.useSalvar()
  const notificar = useNotificacoes()
  const { register, handleSubmit, setError, setValue, formState } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: {
      nome: cliente?.nome ?? '',
      documento: cliente?.documento ?? '',
      email: cliente?.email ?? '',
      telefone: cliente?.telefone ?? '',
    },
  })
  const erros = formState.errors

  const enviar = handleSubmit((dados) =>
    salvar.mutate(
      {
        id: cliente?.id,
        dados: { ...dados, email: dados.email || undefined, telefone: dados.telefone || undefined },
      },
      {
        onSuccess: () => {
          notificar.sucesso(cliente ? 'Cliente atualizado.' : 'Cliente cadastrado.')
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
      <Entrada
        rotulo="CPF ou CNPJ"
        inputMode="text"
        disabled={!!cliente}
        ajuda={cliente ? 'O documento não pode ser alterado.' : 'Aceita CNPJ alfanumérico.'}
        erro={erros.documento?.message}
        {...register('documento', {
          onChange: (e) => setValue('documento', mascararDocumento(e.target.value)),
        })}
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <Entrada rotulo="E-mail" type="email" erro={erros.email?.message} {...register('email')} />
        <Entrada rotulo="Telefone" type="tel" erro={erros.telefone?.message} {...register('telefone')} />
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
