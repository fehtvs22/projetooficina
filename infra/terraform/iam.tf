data "aws_caller_identity" "atual" {}

# ---------------- Papel da instância EC2 ----------------

resource "aws_iam_role" "app" {
  name = "${var.projeto}-ec2"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

# Session Manager (terminal sem SSH) e execução de comandos de deploy
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Baixar as imagens do ECR
resource "aws_iam_role_policy_attachment" "ecr_leitura" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# Ler apenas os segredos deste projeto
resource "aws_iam_role_policy" "segredos" {
  name = "ler-segredos"
  role = aws_iam_role.app.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ssm:GetParameter", "ssm:GetParameters"]
      Resource = "arn:aws:ssm:${var.regiao}:${data.aws_caller_identity.atual.account_id}:parameter/${var.projeto}/*"
    }]
  })
}

resource "aws_iam_instance_profile" "app" {
  name = "${var.projeto}-ec2"
  role = aws_iam_role.app.name
}

# ---------------- GitHub Actions via OIDC (sem chaves de acesso fixas) ----------------

resource "aws_iam_openid_connect_provider" "github" {
  count          = var.criar_provedor_oidc_github ? 1 : 0
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
}

data "aws_iam_openid_connect_provider" "github" {
  count = var.criar_provedor_oidc_github ? 0 : 1
  url   = "https://token.actions.githubusercontent.com"
}

locals {
  oidc_github_arn = var.criar_provedor_oidc_github ? aws_iam_openid_connect_provider.github[0].arn : data.aws_iam_openid_connect_provider.github[0].arn
}

resource "aws_iam_role" "github_deploy" {
  name = "${var.projeto}-github-deploy"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Federated = local.oidc_github_arn }
      Action    = "sts:AssumeRoleWithWebIdentity"
      Condition = {
        StringEquals = {
          "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          # Só o job que usa o environment de produção deste repositório pode assumir o papel
          "token.actions.githubusercontent.com:sub" = "repo:fehtvs22@206714206/projetooficina@1411129260:environment:${var.github_ambiente}"
        }
      }
    }]
  })
}

resource "aws_iam_role_policy" "github_deploy" {
  name = "build-e-deploy"
  role = aws_iam_role.github_deploy.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "LoginEcr"
        Effect   = "Allow"
        Action   = "ecr:GetAuthorizationToken"
        Resource = "*"
      },
      {
        Sid    = "EnviarImagens"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage",
        ]
        Resource = [for r in aws_ecr_repository.imagens : r.arn]
      },
      {
        Sid    = "ComandoDeDeploy"
        Effect = "Allow"
        Action = "ssm:SendCommand"
        Resource = [
          aws_instance.app.arn,
          "arn:aws:ssm:${var.regiao}::document/AWS-RunShellScript",
        ]
      },
      {
        Sid      = "AcompanharComando"
        Effect   = "Allow"
        Action   = ["ssm:GetCommandInvocation", "ssm:ListCommandInvocations"]
        Resource = "*"
      },
    ]
  })
}
