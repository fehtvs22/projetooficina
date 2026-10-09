# Relatório de análise de vulnerabilidades

| | |
|---|---|
| **Ferramenta** | [Trivy](https://trivy.dev) 0.75.0 (Aqua Security) |
| **Data da análise** | 08/10/2026 |
| **Alvos** | 1) Imagem Docker `projetooficina-app` (Alpine 3.24 + JRE Temurin 25 + aplicação) · 2) Repositório (`pom.xml` e dependências transitivas, `Dockerfile`, busca de segredos) |
| **Scanners** | `vuln` (CVEs em pacotes do SO e bibliotecas Java), `secret` (credenciais no código), `misconfig` (boas práticas do Dockerfile) |
| **Resultado final** | ✅ **0 vulnerabilidades** (CRITICAL, HIGH, MEDIUM e LOW) · 0 segredos · 0 problemas de configuração |

Saídas completas do Trivy após as correções: [trivy-imagem.txt](trivy-imagem.txt) e [trivy-repositorio.txt](trivy-repositorio.txt).

## 1. Primeira análise (antes das correções)

A primeira execução encontrou **19 vulnerabilidades**, todas em dependências de terceiros, nenhuma no código da aplicação:

| Severidade | Componente | Versão | CVE(s) | Impacto | Versão corrigida |
|---|---|---|---|---|---|
| CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 11.0.24 | CVE-2026-65182, CVE-2026-65905, CVE-2026-68525 | Bypass de restrições de segurança / autenticação | 11.0.25+ |
| HIGH (5) / MEDIUM (2) | `tools.jackson.core:jackson-core` / `jackson-databind` (Jackson 3) | 3.1.5 | CVE-2026-89407, CVE-2026-89425, CVE-2026-68497, CVE-2026-91776, CVE-2026-91777, CVE-2026-19032, CVE-2026-83557 | Negação de serviço no processamento de JSON | 3.1.7 |
| HIGH (5) / MEDIUM (2) | `com.fasterxml.jackson.core:jackson-core` / `jackson-databind` (Jackson 2, transitivo) | 2.21.5 | mesmas CVEs acima | Negação de serviço no processamento de JSON | 2.21.7 |
| MEDIUM | `libpng` (Alpine) | 1.6.58-r1 | CVE-2026-46675 | Use-after-free | 1.6.59-r0 |
| MEDIUM | `zlib` (Alpine) | 1.3.2-r0 | CVE-2026-85091 | Heap buffer overflow | 1.3.2-r1 |

Essas versões vinham gerenciadas pelo Spring Boot 4.1.1 (a versão mais recente da linha 4.1 na data da análise) e pela imagem base.

## 2. Correções aplicadas

1. **Bibliotecas Java**: sobrescrita das versões gerenciadas no `pom.xml`, sem trocar a versão do Spring Boot:
   ```xml
   <tomcat.version>11.0.26</tomcat.version>
   <jackson-bom.version>3.1.7</jackson-bom.version>
   <jackson-2-bom.version>2.21.7</jackson-2-bom.version>
   ```
   Quando sair um patch do Spring Boot que já traga essas versões, basta atualizar o parent e remover as propriedades.
2. **Imagem Docker**: `apk upgrade --no-cache` no estágio de runtime, aplicando as correções do Alpine (libpng, zlib).
3. A suíte completa (138 testes, unitários + integração + arquitetura) foi executada de novo após as atualizações, sem falhas.

## 3. Segunda análise (após as correções)

| Alvo | Vulnerabilidades | Segredos | Misconfigurations |
|---|---|---|---|
| Imagem `projetooficina-app` (Alpine 3.24.2) | 0 | — | — |
| Imagem — bibliotecas Java do JAR | 0 | — | — |
| `pom.xml` (dependências) | 0 | — | — |
| `Dockerfile` | — | — | 0 |
| Repositório (busca de segredos) | — | 0 | — |

## 4. Controles de segurança da aplicação

| Controle | Implementação |
|---|---|
| Autenticação das APIs administrativas | JWT HS256 (`spring-boot-starter-oauth2-resource-server`), sessão stateless, perfil `ADMIN` exigido em `/api/admin/**` |
| Senhas | Hash BCrypt; nenhum usuário/senha versionado — o admin é criado na inicialização a partir de variáveis de ambiente |
| Segredos | `JWT_SECRET`, `DB_PASSWORD`, `ADMIN_PASSWORD` só por variável de ambiente (`.env` fora do Git); a aplicação não sobe com chave JWT ausente ou menor que 32 caracteres |
| Validação de dados sensíveis | CPF/CNPJ (dígitos verificadores, incluindo CNPJ alfanumérico) e placa (antiga e Mercosul) validados no domínio; Bean Validation nos DTOs |
| Acesso do cliente | O cliente só consulta e responde OS do próprio documento; documento divergente devolve 404 (não revela que a OS existe) |
| Exposição de erros | Respostas RFC 9457 (`application/problem+json`) sem stack trace; mensagens genéricas para violações de integridade |
| Concorrência / integridade | Transação por caso de uso, bloqueio pessimista na baixa de estoque, *optimistic locking* (`@Version`) na OS, constraint `CHECK (quantidade_estoque >= 0)` |
| SQL Injection | Apenas Spring Data JPA / JPQL parametrizado; nenhuma SQL concatenada |
| Container | Imagem JRE mínima (Alpine), usuário não-root `oficina`, `HEALTHCHECK` |
| Superfície exposta | Actuator restrito a `health` e `info`; demais rotas desconhecidas são negadas (`denyAll`) |

## 5. Como reproduzir

```bash
# Imagem (após docker compose build)
trivy image --scanners vuln projetooficina-app:latest

# Repositório: dependências, segredos e Dockerfile
trivy fs --scanners vuln,secret,misconfig --skip-dirs target .
```

O pipeline de CI (`.github/workflows/ci.yml`) roda o `trivy fs` a cada push/PR e falha se surgir vulnerabilidade CRITICAL ou HIGH com correção disponível.
