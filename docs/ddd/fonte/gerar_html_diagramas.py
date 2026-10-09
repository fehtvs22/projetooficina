"""Gera fonte/diagramas.html a partir de diagramas.md (para exportar em PDF). Uso: python3 fonte/gerar_html_diagramas.py"""
import html
import re
from pathlib import Path

base = Path(__file__).resolve().parent.parent
md = (base / "diagramas.md").read_text(encoding="utf-8")


def inline(texto):
    texto = html.escape(texto.strip())
    texto = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", texto)
    texto = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", texto)
    return re.sub(r"`([^`]+)`", r"<code>\1</code>", texto)


def tabela(texto):
    linhas = [l for l in texto.splitlines() if l.startswith("|")]
    if not linhas:
        return ""
    out = "<table>"
    for i, linha in enumerate(linhas):
        if i == 1:
            continue
        tag = "th" if i == 0 else "td"
        out += "<tr>" + "".join(f"<{tag}>{inline(c)}</{tag}>" for c in linha.strip("|").split("|")) + "</tr>"
    return out + "</table>"


secoes = []
for parte in re.split(r"^## ", md, flags=re.M)[1:]:
    titulo, _, corpo = parte.partition("\n")
    m = re.search(r"```mermaid\n(.*?)```", corpo, re.S)
    diagrama = f'<pre class="mermaid">{html.escape(m.group(1))}</pre>' if m else ""
    texto = re.sub(r"```mermaid\n.*?```", "", corpo, flags=re.S)
    paragrafos = "".join(f"<p>{inline(p)}</p>" for p in texto.split("\n\n")
                         if p.strip() and not p.strip().startswith("|"))
    secoes.append(f"<section><h2>{html.escape(titulo)}</h2>{paragrafos}{diagrama}{tabela(texto)}</section>")

(base / "fonte" / "diagramas.html").write_text(f"""<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<title>Diagramas DDD — Oficina Mecânica</title>
<style>@page{{size:A4 landscape;margin:12mm}}
body{{font-family:-apple-system,"Segoe UI",Roboto,Arial,sans-serif;color:#1d1d1f;font-size:12px}}
h1{{font-size:24px}}h2{{font-size:18px;border-bottom:2px solid #333;padding-bottom:4px}}
section{{page-break-after:always}}section:last-child{{page-break-after:auto}}
table{{border-collapse:collapse;width:100%;margin-top:10px}}td,th{{border:1px solid #bbb;padding:4px 6px;text-align:left;vertical-align:top}}
th{{background:#eee}}code{{font-family:Menlo,Consolas,monospace;font-size:10.5px}}
.mermaid{{display:flex;justify-content:center}}.mermaid svg{{max-height:150mm;max-width:100%}}</style>
<script src="https://cdn.jsdelivr.net/npm/mermaid@11.4.1/dist/mermaid.min.js"></script>
<script>mermaid.initialize({{startOnLoad:true,theme:"default",securityLevel:"loose"}});</script></head>
<body><h1>Diagramas DDD — Sistema Integrado de Atendimento e Execução de Serviços</h1>{"".join(secoes)}</body></html>""",
                                               encoding="utf-8")
print("fonte/diagramas.html gerado")
