#!/bin/bash
# Backup diário do PostgreSQL (agende com cron na EC2; veja o passo 9 do guia).
set -euo pipefail
cd /opt/oficina
arquivo="backups/oficina-$(date +%F-%H%M).sql.gz"
docker compose exec -T postgres pg_dump -U oficina oficina | gzip > "$arquivo"
find backups -name '*.sql.gz' -mtime +7 -delete
echo "Backup gerado: $arquivo"
