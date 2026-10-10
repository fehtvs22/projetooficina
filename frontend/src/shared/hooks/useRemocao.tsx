import type { UseMutationResult } from '@tanstack/react-query'
import { mensagemDeErro } from '@/shared/api/http'
import { useConfirmacao, useNotificacoes } from '@/shared/ui'

/** Pede confirmação, executa a remoção e informa o resultado. */
export function useRemocao(mutacao: UseMutationResult<void, Error, string>) {
  const confirmar = useConfirmacao()
  const notificar = useNotificacoes()
  return async (id: string, descricao: string) => {
    const ok = await confirmar({
      titulo: 'Confirmar remoção',
      mensagem: (
        <>
          Remover <strong>{descricao}</strong>? Esta ação não pode ser desfeita.
        </>
      ),
      confirmar: 'Remover',
      perigo: true,
    })
    if (!ok) return
    mutacao.mutate(id, {
      onSuccess: () => notificar.sucesso(`${descricao} removido.`),
      onError: (erro) => notificar.erro(mensagemDeErro(erro)),
    })
  }
}
