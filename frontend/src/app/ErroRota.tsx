import { isRouteErrorResponse, Link, useRouteError } from 'react-router'

export function ErroRota() {
  const erro = useRouteError()
  const naoEncontrada = isRouteErrorResponse(erro) && erro.status === 404
  if (!naoEncontrada) console.error(erro)
  return (
    <main className="flex min-h-dvh flex-col items-center justify-center gap-3 px-4 text-center">
      <h1 className="text-2xl font-semibold">
        {naoEncontrada ? 'Página não encontrada' : 'Algo deu errado'}
      </h1>
      <p className="text-slate-500">
        {naoEncontrada
          ? 'O endereço acessado não existe.'
          : 'Ocorreu um erro inesperado. Tente recarregar a página.'}
      </p>
      <Link to="/" className="font-medium text-marca-600 hover:underline">
        Voltar ao início
      </Link>
    </main>
  )
}
