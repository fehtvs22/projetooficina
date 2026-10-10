terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Estado remoto (recomendado). Veja o passo 3 de docs/guias/deploy-aws.md.
  # backend "s3" {
  #   bucket       = "SEU-BUCKET-DE-ESTADO"
  #   key          = "oficina/terraform.tfstate"
  #   region       = "us-east-1"
  #   use_lockfile = true
  #   encrypt      = true
  # }
}

provider "aws" {
  region = var.regiao

  default_tags {
    tags = {
      Projeto    = var.projeto
      Ambiente   = "producao"
      Gerenciado = "terraform"
    }
  }
}
