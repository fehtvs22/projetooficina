locals {
  repositorios = toset(["${var.projeto}-api", "${var.projeto}-frontend"])
}

resource "aws_ecr_repository" "imagens" {
  for_each             = local.repositorios
  name                 = each.value
  image_tag_mutability = "IMMUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

# Mantém só as 10 imagens mais recentes de cada repositório (controle de custo)
resource "aws_ecr_lifecycle_policy" "imagens" {
  for_each   = aws_ecr_repository.imagens
  repository = each.value.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Manter as 10 imagens mais recentes"
      selection    = { tagStatus = "any", countType = "imageCountMoreThan", countNumber = 10 }
      action       = { type = "expire" }
    }]
  })
}
