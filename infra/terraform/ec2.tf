# AMI oficial do Ubuntu 24.04 LTS publicada pela Canonical no SSM Parameter Store
data "aws_ssm_parameter" "ubuntu" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

resource "aws_instance" "app" {
  ami                    = data.aws_ssm_parameter.ubuntu.value
  instance_type          = var.tipo_instancia
  iam_instance_profile   = aws_iam_instance_profile.app.name
  vpc_security_group_ids = [aws_security_group.app.id]
  user_data              = file("${path.module}/user_data.sh")

  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.tamanho_disco_gb
    encrypted             = true
    delete_on_termination = true
  }

  # IMDSv2 obrigatório (protege as credenciais da instância contra SSRF)
  metadata_options {
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  # O banco mora no disco desta instância: uma AMI nova ou mudança no user_data
  # NÃO podem recriar a máquina (isso apagaria os dados).
  lifecycle {
    ignore_changes = [ami, user_data]
  }

  tags = { Name = "${var.projeto}-app" }
}

# IP fixo: não muda quando a instância é parada/iniciada
resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"
  tags     = { Name = "${var.projeto}-app" }
}
