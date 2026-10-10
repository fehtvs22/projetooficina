import { Selo } from '@/shared/ui'
import {
  COR_SITUACAO_ORCAMENTO,
  COR_STATUS,
  ROTULO_SITUACAO_ORCAMENTO,
  ROTULO_STATUS,
} from '../dominio/status'
import type { SituacaoOrcamento, StatusOS } from '../tipos'

export function SeloStatus({ status }: { status: StatusOS }) {
  return <Selo cor={COR_STATUS[status]}>{ROTULO_STATUS[status]}</Selo>
}

export function SeloOrcamento({ situacao }: { situacao: SituacaoOrcamento }) {
  return <Selo cor={COR_SITUACAO_ORCAMENTO[situacao]}>{ROTULO_SITUACAO_ORCAMENTO[situacao]}</Selo>
}
