# Segredos gerados pelo Terraform e guardados como SecureString no SSM Parameter Store.
# A instância lê esses valores no deploy; nada fica no repositório.
# Atenção: os valores também ficam no estado do Terraform, por isso o estado precisa ser protegido (passo 3 do guia).

resource "random_password" "db" {
  length  = 32
  special = false
}

resource "random_password" "jwt" {
  length  = 64
  special = false
}

resource "random_password" "admin" {
  length           = 20
  special          = true
  override_special = "!@#%*-_"
}

locals {
  segredos = {
    "db-password"    = random_password.db.result
    "jwt-secret"     = random_password.jwt.result
    "admin-password" = random_password.admin.result
  }
}

resource "aws_ssm_parameter" "segredos" {
  for_each = local.segredos
  name     = "/${var.projeto}/${each.key}"
  type     = "SecureString"
  value    = each.value
}

resource "aws_ssm_parameter" "admin_usuario" {
  name  = "/${var.projeto}/admin-username"
  type  = "String"
  value = var.admin_usuario
}
