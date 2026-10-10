export function CampoBusca({
  valor,
  aoMudar,
  placeholder,
}: {
  valor: string
  aoMudar: (v: string) => void
  placeholder: string
}) {
  return (
    <input
      type="search"
      value={valor}
      onChange={(e) => aoMudar(e.target.value)}
      placeholder={placeholder}
      aria-label={placeholder}
      className="h-9 w-full max-w-xs rounded-lg border border-slate-300 bg-white px-3 text-sm placeholder:text-slate-400"
    />
  )
}
