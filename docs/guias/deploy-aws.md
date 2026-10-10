# Deploy na AWS: GitHub → Terraform → EC2

Passo a passo para publicar **banco + API + front-end** numa única EC2, com deploy automático a cada push na `main`.

## Visão geral

```
 git push (main)
      │
      ▼
 GitHub Actions ── CI: testes, cobertura, Trivy ──► (passou?)
      │                                                │ sim
      ▼                                                ▼
 Deploy: build das imagens ──► Amazon ECR ──► SSM Run Command na EC2
                                                     │
                     EC2 t3.small (Ubuntu 24.04, 2 GB + 2 GB swap)
                     ┌──────────────── docker compose ─────────────────┐
  navegador ──:80──► │ frontend (Nginx) ──/api──► app (Spring) ──► postgres │
                     └──────────────────────────────────────────────────┘
```

| Peça | Papel |
|---|---|
| **Terraform** (`infra/terraform`) | Cria a EC2, IP fixo, firewall, repositórios ECR, segredos e permissões. Roda **uma vez** da sua máquina (e quando a infra mudar). |
| **GitHub Actions** (`.github/workflows/deploy.yml`) | Depois do CI passar, constrói as imagens, envia ao ECR e manda a EC2 atualizar. |
| **EC2** (`deploy/`) | Só **baixa e executa** as imagens. Nada é compilado no servidor. |

Decisões de segurança: **sem SSH e sem chaves de acesso fixas.** O terminal da EC2 é pelo SSM Session Manager (porta 22
fechada). O GitHub autentica na AWS por **OIDC** (credenciais temporárias). As senhas são geradas pelo Terraform e ficam
no **SSM Parameter Store**, nunca no repositório.

### Uma EC2 de 2 GB aguenta?

**Sim, para este projeto, desde que ela não compile as imagens.** O build do Maven e do Vite é o que estoura 2 GB, e
por isso ele roda no GitHub Actions. Na EC2, cada container tem limite de memória (`deploy/docker-compose.prod.yml`):

| Container | Limite | Uso medido em repouso |
|---|---|---|
| API (Spring Boot) | 768 MB | ~270 MB |
| PostgreSQL | 384 MB | ~45 MB |
| Front (Nginx) | 64 MB | ~5 MB |

Ainda sobram ~500 MB para o sistema, e há 2 GB de swap de reserva. Use a **t3.small** (2 vCPU / 2 GB). A t3.micro
(1 GB) é pequena demais para a JVM junto com o PostgreSQL.

### Custo estimado (us-east-1, sob demanda)

| Item | US$/mês |
|---|---|
| EC2 t3.small (24×7) | ~15,20 |
| Disco gp3 20 GB | ~1,60 |
| IPv4 público (Elastic IP) | ~3,65 |
| ECR (≤ 10 imagens por repositório) | < 0,50 |
| **Total** | **~21** |

Contas novas recebem créditos do plano gratuito da AWS; confira o saldo em *Billing → Credits*. Em `sa-east-1`
(São Paulo) a latência é menor, mas a EC2 custa ~50% a mais. Para não pagar quando não estiver usando, veja o
[passo 10](#10-desligar-ou-destruir).

---

## 1. Pré-requisitos (uma vez)

**Na AWS**

1. Crie a conta e **ative MFA no usuário root**. Não use o root no dia a dia.
2. Crie um **alerta de orçamento**: *Billing → Budgets → Create budget → Monthly cost budget*, por exemplo US$ 25.
3. Crie um acesso para o Terraform. O mais simples é um usuário IAM com a política `AdministratorAccess` e uma
   *access key* (IAM → Users → Create user → *Security credentials* → *Create access key* → *CLI*).
   Alternativa mais segura: IAM Identity Center (SSO) com `aws configure sso`.

**Na sua máquina (macOS)**

```bash
brew install hashicorp/tap/terraform jq
brew install --cask session-manager-plugin   # terminal da EC2 sem SSH
# AWS CLI e gh você já tem

aws configure            # cole a access key, região us-east-1, saída json
aws sts get-caller-identity   # deve mostrar sua conta (e não um erro)
gh auth login            # se ainda não estiver logado no GitHub
```

> A AWS CLI desta máquina está com uma credencial **inválida** (`InvalidClientTokenId`). Rode o `aws configure`
> com uma chave nova antes de continuar.

## 2. Configurar o Terraform

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
```

Confira o `terraform.tfvars`:

```hcl
github_repositorio = "fehtvs22/projetooficina"   # dono/repositório que pode fazer deploy
regiao             = "us-east-1"
tipo_instancia     = "t3.small"
criar_provedor_oidc_github = true                 # false se a conta já tiver o OIDC do GitHub
```

## 3. (Recomendado) Estado remoto no S3

O estado do Terraform guarda **as senhas geradas** e o mapa do que existe na AWS. Se ele ficar só no seu
notebook e você perdê-lo, o Terraform não consegue mais gerenciar a infraestrutura. Guarde o estado num bucket S3
privado, versionado e criptografado:

```bash
BUCKET=oficina-tfstate-$(aws sts get-caller-identity --query Account --output text)
aws s3api create-bucket --bucket "$BUCKET" --region us-east-1
aws s3api put-bucket-versioning --bucket "$BUCKET" --versioning-configuration Status=Enabled
aws s3api put-public-access-block --bucket "$BUCKET" \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
echo "$BUCKET"
```

Depois descomente o bloco `backend "s3"` em `versions.tf` e troque `SEU-BUCKET-DE-ESTADO` pelo nome impresso.
(Se pular este passo, o estado fica em `infra/terraform/terraform.tfstate`. **Não faça commit desse arquivo**; o
`.gitignore` já o ignora.)

## 4. Criar a infraestrutura

```bash
cd infra/terraform
terraform init
terraform plan      # revise: ~25 recursos a criar, nada a destruir
terraform apply     # digite "yes"
```

Leva ~2 minutos. No fim aparecem os *outputs*, por exemplo:

```
url              = "http://3.91.xx.xx"
instancia_id     = "i-0abc..."
github_variaveis = { AWS_REGION = "us-east-1", AWS_ROLE_ARN = "arn:aws:iam::...:role/oficina-github-deploy", ... }
```

Faça commit do `.terraform.lock.hcl` gerado (ele fixa as versões dos providers).

A EC2 ainda leva **~3 minutos** instalando Docker e criando o swap. Para acompanhar:

```bash
aws ssm start-session --target "$(terraform output -raw instancia_id)"
# dentro da sessão:
cloud-init status --wait     # espere "status: done"
exit
```

## 5. Ligar o GitHub à AWS

Cadastre os *outputs* como **Variables** do repositório. Não são segredos, porque o OIDC dispensa chaves:

```bash
cd infra/terraform
terraform output -json github_variaveis | jq -r 'to_entries[] | "\(.key) \(.value)"' |
  while read -r chave valor; do gh variable set "$chave" --body "$valor" --repo fehtvs22/projetooficina; done

gh variable list --repo fehtvs22/projetooficina   # confira as 4 variáveis
```

**(Opcional, recomendado)** Em *GitHub → Settings → Environments → producao*, adicione **Required reviewers**:
assim cada deploy espera sua aprovação. O environment `producao` é criado automaticamente no primeiro deploy.

## 6. Primeiro deploy

Envie o código para a `main`:

```bash
cd ../..   # raiz do repositório
git add .
git commit -m "Front-end, infraestrutura AWS e deploy automatizado"
git push origin main
```

Na aba **Actions**: o workflow **CI** roda (testes do back e do front, Trivy) e, se passar, dispara o **Deploy**,
que leva ~5–8 min na primeira vez e menos nas seguintes, graças ao cache. Para disparar o deploy manualmente:

```bash
gh workflow run Deploy --repo fehtvs22/projetooficina
gh run watch --repo fehtvs22/projetooficina
```

O passo *Deploy na EC2 via SSM* mostra a saída do servidor e termina com `API saudável`.

## 7. Acessar

```bash
cd infra/terraform
terraform output url                            # abra no navegador
eval "$(terraform output -raw comando_senha_admin)"   # senha do usuário admin
```

- Oficina: `http://<ip>/login`, usuário `admin` e a senha acima.
- Cliente: `http://<ip>/acompanhamento`.
- Swagger e health **não** ficam expostos: o Nginx só encaminha `/api`.

A senha do admin vale a partir do **primeiro** boot da API. Trocar o parâmetro depois não altera o usuário que já existe.

## 8. Operação do dia a dia

**Terminal na EC2 (sem SSH):**

```bash
aws ssm start-session --target "$(terraform -chdir=infra/terraform output -raw instancia_id)"
sudo -i
cd /opt/oficina
docker compose ps                    # estado dos containers
docker compose logs -f --tail 100 app   # logs da API
docker stats --no-stream             # memória por container
free -h                              # RAM e swap
```

**Novo deploy:** basta `git push` na `main`.

**Rollback** para uma versão anterior. As imagens são marcadas com o SHA do commit, e o ECR guarda as 10 últimas:

```bash
# Opção 1: reverter o commit e fazer push (passa pelo CI e gera novo deploy)
git revert <sha-ruim> && git push

# Opção 2 (imediato): dentro da sessão SSM, apontar para a imagem antiga
sudo REGISTRY=<conta>.dkr.ecr.us-east-1.amazonaws.com IMAGE_TAG=<sha-anterior> AWS_REGION=us-east-1 \
  /opt/oficina/deploy.sh
```

## 9. Backup do banco

O PostgreSQL grava no disco da EC2. O Terraform está configurado para **nunca recriar a instância** por causa de AMI
nova ou `user_data` (`ignore_changes`), mas backup continua sendo obrigatório. Na sessão SSM:

```bash
sudo crontab -e
# backup diário às 3h (guarda 7 dias em /opt/oficina/backups)
0 3 * * * /opt/oficina/backup.sh >> /var/log/oficina-backup.log 2>&1
```

Para restaurar:

```bash
cd /opt/oficina
gunzip -c backups/<arquivo>.sql.gz | docker compose exec -T postgres psql -U oficina oficina
```

Para guardar fora da máquina, copie os arquivos para um bucket S3 ou ative snapshots diários do disco em
*EC2 → Lifecycle Manager*.

## 10. Desligar ou destruir

- **Pausar** (para de cobrar a EC2; o disco e o IP continuam cobrando ~US$ 5/mês):
  `aws ec2 stop-instances --instance-ids <id>`. Depois, `start-instances`. O IP não muda (Elastic IP).
- **Destruir tudo** (apaga os dados; faça backup antes):

  ```bash
  cd infra/terraform
  terraform destroy
  ```

## Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `EntityAlreadyExists` no OIDC durante o `apply` | A conta já tem o provedor do GitHub: use `criar_provedor_oidc_github = false`. |
| `no matching EC2 VPC found` | A conta não tem VPC padrão: `aws ec2 create-default-vpc`. |
| Deploy: `Not authorized to perform sts:AssumeRoleWithWebIdentity` | `github_repositorio` no tfvars difere do repositório real, ou o job não usa o environment `producao`. |
| Deploy: `InvalidInstanceId` | O agente SSM ainda não registrou a EC2 (espere ~3 min após o `apply`) ou `EC2_INSTANCE_ID` está errado. |
| Deploy: `docker: command not found` | O deploy rodou antes de o `user_data` terminar. Espere `cloud-init status --wait` e rode o deploy de novo. |
| Site responde **502** | A API ainda está subindo (~30–60 s) ou caiu: `docker compose logs app`. |
| Lentidão, `free -h` mostra swap alto | Considere `t3.medium` (4 GB): mude `tipo_instancia` e rode `terraform apply`. A troca de tipo **não** recria a máquina, só a reinicia. |

## Próximos passos (fora deste guia)

- **HTTPS:** exige um domínio. Aponte um registro A para o IP e coloque um proxy com certificado automático (por
  exemplo, [Caddy](https://caddyserver.com/docs/automatic-https)) na frente do Nginx, publicando as portas 80/443. A
  porta 443 já está liberada no firewall.
- **Banco gerenciado (RDS):** backups, *point-in-time recovery* e atualizações automáticas, a partir de ~US$ 15/mês a mais.
- **Alta disponibilidade:** mais de uma instância com Load Balancer exige tirar o banco da EC2 (RDS) e passa de US$ 50/mês.
