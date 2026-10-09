#!/usr/bin/env bash
# Exporta a documentação DDD para PDF usando o Chrome/Chromium em modo headless.
set -euo pipefail
cd "$(dirname "$0")/.."
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"
python3 fonte/gerar_html_diagramas.py
for nome in event-storming diagramas; do
  "$CHROME" --headless=new --disable-gpu --no-pdf-header-footer --virtual-time-budget=15000 \
    --print-to-pdf="$PWD/$nome.pdf" "file://$PWD/fonte/$nome.html" 2>/dev/null
  echo "$nome.pdf gerado"
done
