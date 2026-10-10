output "url" {
  description = "Endereço da aplicação"
  value       = "http://${aws_eip.app.public_ip}"
}

output "ip_publico" {
  value = aws_eip.app.public_ip
}

output "instancia_id" {
  description = "Use em: aws ssm start-session --target <id>"
  value       = aws_instance.app.id
}

output "ecr_registro" {
  value = split("/", aws_ecr_repository.imagens["${var.projeto}-api"].repository_url)[0]
}

output "github_variaveis" {
  description = "Cadastre como Variables do repositório no GitHub (passo 6 do guia)"
  value = {
    AWS_REGION      = var.regiao
    AWS_ROLE_ARN    = aws_iam_role.github_deploy.arn
    EC2_INSTANCE_ID = aws_instance.app.id
    PROJETO         = var.projeto
  }
}

output "comando_senha_admin" {
  description = "Mostra a senha do administrador"
  value       = "aws ssm get-parameter --region ${var.regiao} --name /${var.projeto}/admin-password --with-decryption --query Parameter.Value --output text"
}
