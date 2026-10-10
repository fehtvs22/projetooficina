import { Navigate, Outlet, useLocation } from 'react-router'
import { useSessao } from './sessao'

export function RotaProtegida() {
  const sessao = useSessao()
  const local = useLocation()
  if (!sessao) {
    const destino = local.pathname + local.search
    return <Navigate to={`/login?destino=${encodeURIComponent(destino)}`} replace />
  }
  return <Outlet />
}
