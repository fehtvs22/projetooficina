# Usa a VPC padrão da conta: suficiente para uma única instância e sem custo de NAT Gateway.
data "aws_vpc" "padrao" {
  default = true
}

resource "aws_security_group" "app" {
  name        = "${var.projeto}-app"
  description = "Entrada HTTP/HTTPS publica; sem SSH (acesso administrativo via SSM Session Manager)"
  vpc_id      = data.aws_vpc.padrao.id
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.app.id
  description       = "HTTP"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
}

resource "aws_vpc_security_group_ingress_rule" "https" {
  security_group_id = aws_security_group.app.id
  description       = "HTTPS (quando houver dominio)"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
}

resource "aws_vpc_security_group_egress_rule" "https" {
  security_group_id = aws_security_group.app.id
  description       = "Saida HTTPS para atualizacoes, ECR e SSM"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
}
