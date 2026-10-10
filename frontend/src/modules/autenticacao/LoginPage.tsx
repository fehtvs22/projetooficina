import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router'
import { z } from 'zod'
import { mensagemDeErro } from '@/shared/api/http'
import { Alerta, Botao, Entrada } from '@/shared/ui'
import { autenticacaoApi } from './api'
import { sessao, useSessao } from './sessao'

const esquema = z.object({
  usuario: z.string().trim().min(1, 'Informe o usuário'),
  senha: z.string().min(1, 'Informe a senha'),
})
type Formulario = z.infer<typeof esquema>

/** Aceita só caminhos internos, evitando open redirect via ?destino=https://... */
function destinoSeguro(destino: string | null): string {
  return destino && destino.startsWith('/') && !destino.startsWith('//') ? destino : '/'
}

export default function LoginPage() {
  const ativa = useSessao()
  const navegar = useNavigate()
  const [params] = useSearchParams()
  const destino = destinoSeguro(params.get('destino'))

  const { register, handleSubmit, formState } = useForm<Formulario>({ resolver: zodResolver(esquema) })
  const login = useMutation({
    mutationFn: autenticacaoApi.login,
    onSuccess: (token, credenciais) => {
      sessao.iniciar(token.token, credenciais.usuario, token.expiraEmSegundos)
      navegar(destino, { replace: true })
    },
  })

  if (ativa) return <Navigate to={destino} replace />

  return (
    <main className="flex min-h-dvh items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex items-center justify-center gap-2">
          <img src="/favicon.svg" alt="" className="size-9" />
          <span className="text-lg font-semibold">Oficina Mecânica</span>
        </div>
        <form
          onSubmit={handleSubmit((dados) => login.mutate(dados))}
          className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
          noValidate
        >
          <div>
            <h1 className="text-lg font-semibold">Acesso da oficina</h1>
            <p className="text-sm text-slate-500">Entre com seu usuário administrativo.</p>
          </div>
          {login.isError && <Alerta tipo="erro">{mensagemDeErro(login.error)}</Alerta>}
          <Entrada
            rotulo="Usuário"
            autoComplete="username"
            autoFocus
            erro={formState.errors.usuario?.message}
            {...register('usuario')}
          />
          <Entrada
            rotulo="Senha"
            type="password"
            autoComplete="current-password"
            erro={formState.errors.senha?.message}
            {...register('senha')}
          />
          <Botao type="submit" className="w-full" carregando={login.isPending}>
            Entrar
          </Botao>
        </form>
        <p className="mt-6 text-center text-sm text-slate-500">
          É cliente?{' '}
          <Link to="/acompanhamento" className="font-medium text-marca-600 hover:underline">
            Acompanhe sua ordem de serviço
          </Link>
        </p>
      </div>
    </main>
  )
}
