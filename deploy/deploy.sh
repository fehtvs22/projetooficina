#!/bin/bash
# Executado NA EC2 (via SSM) pelo workflow de deploy.
# Uso: REGISTRY=... IMAGE_TAG=... AWS_REGION=... PROJETO=oficina ./deploy.sh
set -euo pipefail

: "${REGISTRY:?}" "${IMAGE_TAG:?}" "${AWS_REGION:?}"
PROJETO="${PROJETO:-oficina}"
cd /opt/oficina

parametro() {
  aws ssm get-parameter --region "$AWS_REGION" --name "/$PROJETO/$1" --with-decryption \
    --query Parameter.Value --output text
}

echo ">> Gerando .env a partir do SSM Parameter Store"
umask 077
cat > .env <<ENV
REGISTRY=$REGISTRY
IMAGE_TAG=$IMAGE_TAG
DB_USERNAME=oficina
DB_PASSWORD=$(parametro db-password)
JWT_SECRET=$(parametro jwt-secret)
ADMIN_USERNAME=$(parametro admin-username)
ADMIN_PASSWORD=$(parametro admin-password)
ENV

echo ">> Login no ECR"
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY"

echo ">> Baixando imagens ($IMAGE_TAG)"
docker compose pull --quiet

echo ">> Subindo containers"
docker compose up -d --remove-orphans

echo ">> Aguardando a API ficar saudável"
for _ in $(seq 1 40); do
  estado=$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q app)" 2>/dev/null || echo "iniciando")
  if [ "$estado" = "healthy" ]; then
    echo ">> API saudável"
    docker compose ps
    docker image prune -af --filter "until=168h" > /dev/null
    exit 0
  fi
  sleep 5
done

echo "!! A API não ficou saudável em 200s. Últimos logs:" >&2
docker compose logs --tail 80 app >&2
exit 1
