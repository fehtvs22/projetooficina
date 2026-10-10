#!/bin/bash
# Preparação da instância (executa só no primeiro boot).
set -euxo pipefail
export DEBIAN_FRONTEND=noninteractive

# Swap de 2 GB: margem de segurança para os 2 GB de RAM
if [ ! -f /swapfile ]; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  echo 'vm.swappiness=10' > /etc/sysctl.d/99-swap.conf
  sysctl --system
fi

# Docker + Compose (pacotes do Ubuntu) e AWS CLI
apt-get update
apt-get install -y docker.io docker-compose-v2 unattended-upgrades
snap install aws-cli --classic
systemctl enable --now docker

# Atualizações de segurança automáticas
dpkg-reconfigure -f noninteractive unattended-upgrades

mkdir -p /opt/oficina/backups
