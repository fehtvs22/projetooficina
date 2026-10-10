import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { mensagemDeErro } from '@/shared/api/http'
import { aplicarErrosDaApi } from '@/shared/forms/errosApi'
import { Alerta, AreaTexto, Botao, Entrada, Selecao, useNotificacoes } from '@/shared/ui'
import { pecas, TIPOS_PECA, type Peca } from './api'

const inteiroNaoNegativo = (mensagem: string) =>
  z.number({ error: mensagem }).int().min(0, 'Não pode ser negativo')

const esquema = z.object({
  codigo: z.string().trim().min(1, 'Informe o código').max(40),
  nome: z.string().trim().min(1, 'Informe o nome').max(120),
  descricao: z.string().trim().max(500),
  tipo: z.enum(['PECA', 'INSUMO']),
  preco: z.number({ error: 'Informe o preço' }).min(0, 'O preço não pode ser negativo'),
  quantidadeInicial: inteiroNaoNegativo('Informe o saldo inicial'),
  estoqueMinimo: inteiroNaoNegativo('Informe o estoque mínimo'),
})
type Formulario = z.infer<typeof esquema>

export function FormularioPeca({ peca, aoConcluir }: { peca?: Peca; aoConcluir: () => void }) {
  const salvar = pecas.useSalvar()
  const notificar = useNotificacoes()
  const { register, handleSubmit, setError, formState } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: peca
      ? { ...peca, descricao: peca.descricao ?? '', quantidadeInicial: peca.quantidadeEstoque }
      : { codigo: '', nome: '', descricao: '', tipo: 'PECA', quantidadeInicial: 0, estoqueMinimo: 0 },
  })
  const erros = formState.errors

  const enviar = handleSubmit((dados) =>
    salvar.mutate(
      { id: peca?.id, dados: { ...dados, descricao: dados.descricao || undefined } },
      {
        onSuccess: () => {
          notificar.sucesso(peca ? 'Item atualizado.' : 'Item cadastrado.')
          aoConcluir()
        },
        onError: (erro) => aplicarErrosDaApi(erro, setError),
      },
    ),
  )

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      {salvar.isError && <Alerta tipo="erro">{mensagemDeErro(salvar.error)}</Alerta>}
      <div className="grid gap-4 sm:grid-cols-[1fr_2fr]">
        <Entrada
          rotulo="Código"
          autoFocus
          className="uppercase"
          erro={erros.codigo?.message}
          {...register('codigo')}
        />
        <Entrada rotulo="Nome" erro={erros.nome?.message} {...register('nome')} />
      </div>
      <AreaTexto rotulo="Descrição" rows={2} erro={erros.descricao?.message} {...register('descricao')} />
      <div className="grid gap-4 sm:grid-cols-2">
        <Selecao rotulo="Tipo" erro={erros.tipo?.message} {...register('tipo')}>
          {Object.entries(TIPOS_PECA).map(([valor, rotulo]) => (
            <option key={valor} value={valor}>
              {rotulo}
            </option>
          ))}
        </Selecao>
        <Entrada
          rotulo="Preço (R$)"
          type="number"
          step="0.01"
          min="0"
          erro={erros.preco?.message}
          {...register('preco', { valueAsNumber: true })}
        />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Entrada
          rotulo={peca ? 'Saldo atual' : 'Saldo inicial'}
          type="number"
          min="0"
          disabled={!!peca}
          ajuda={peca ? 'Use entrada/saída de estoque para ajustar.' : undefined}
          erro={erros.quantidadeInicial?.message}
          {...register('quantidadeInicial', { valueAsNumber: true })}
        />
        <Entrada
          rotulo="Estoque mínimo"
          type="number"
          min="0"
          erro={erros.estoqueMinimo?.message}
          {...register('estoqueMinimo', { valueAsNumber: true })}
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
