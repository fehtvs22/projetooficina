import { useDeferredValue, useMemo, useState } from 'react'

function normalizar(texto: string) {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}

/** Filtro textual no cliente (as listas do MVP são pequenas e a API não pagina). */
export function useFiltro<T>(itens: T[] | undefined, campos: (item: T) => Array<string | null | undefined>) {
  const [termo, setTermo] = useState('')
  const adiado = useDeferredValue(termo)
  const filtrados = useMemo(() => {
    const busca = normalizar(adiado.trim())
    if (!itens || !busca) return itens ?? []
    return itens.filter((item) => campos(item).some((c) => c && normalizar(c).includes(busca)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [itens, adiado])
  return { termo, setTermo, filtrados }
}
