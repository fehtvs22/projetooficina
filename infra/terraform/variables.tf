variable "projeto" {
  description = "Prefixo dos recursos"
  type        = string
  default     = "oficina"
}

variable "regiao" {
  description = "Região AWS (us-east-1 é a mais barata; sa-east-1 tem menor latência no Brasil)"
  type        = string
  default     = "us-east-1"
}

variable "tipo_instancia" {
  description = "Tipo da EC2. t3.small = 2 vCPU / 2 GB"
  type        = string
  default     = "t3.small"
}

variable "tamanho_disco_gb" {
  description = "Disco raiz (sistema, imagens Docker e dados do PostgreSQL)"
  type        = number
  default     = 20
}

variable "github_repositorio" {
  description = "Repositório autorizado a fazer deploy, no formato dono/repositorio"
  type        = string
}

variable "github_ambiente" {
  description = "Environment do GitHub Actions usado pelo job de deploy"
  type        = string
  default     = "producao"
}

variable "criar_provedor_oidc_github" {
  description = "false se a conta AWS já tiver o provedor OIDC token.actions.githubusercontent.com"
  type        = bool
  default     = true
}

variable "admin_usuario" {
  description = "Usuário administrador criado na primeira inicialização da API"
  type        = string
  default     = "admin"
}
