import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState, type ReactNode } from 'react'
import { ApiError } from '@/shared/api/http'
import { ProvedorConfirmacao, ProvedorNotificacoes } from '@/shared/ui'

function criarQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        refetchOnWindowFocus: true,
        // Erros 4xx não melhoram com nova tentativa; só repetimos falhas de rede/servidor.
        retry: (tentativas, erro) =>
          !(erro instanceof ApiError && erro.status >= 400 && erro.status < 500) && tentativas < 2,
      },
      mutations: { retry: false },
    },
  })
}

export function Provedores({ children }: { children: ReactNode }) {
  const [queryClient] = useState(criarQueryClient)
  return (
    <QueryClientProvider client={queryClient}>
      <ProvedorNotificacoes>
        <ProvedorConfirmacao>{children}</ProvedorConfirmacao>
      </ProvedorNotificacoes>
    </QueryClientProvider>
  )
}
