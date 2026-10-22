# ICOL System - Backend API

API de gestão acadêmica para a ONG ICOL, desenvolvida com Spring Boot.

## Tecnologias

- Java 21 e Spring Boot 3
- Spring Data JPA / Hibernate
- Spring Security com JWT, Argon2id e autorização por perfil
- PostgreSQL com migrations Flyway
- Testcontainers para testes de integração
- Maven

## Configuração local

Configure as variáveis de ambiente antes de iniciar:

```env
DB_URL=jdbc:postgresql://localhost:5432/icol_db
DB_USERNAME=postgres
DB_PASSWORD=<senha-do-banco>
JWT_SECRET=<segredo-aleatorio-com-no-minimo-32-bytes>
JWT_EXPIRATION=3600000
ICOL_ADMIN_EMAIL=<email-da-coordenadora-inicial>
ICOL_ADMIN_PASSWORD=<senha-com-no-minimo-8-caracteres>
```

`JWT_SECRET` é obrigatório. A aplicação falha ao iniciar se o segredo estiver
ausente ou tiver menos de 32 bytes. `JWT_EXPIRATION` é expresso em
milissegundos; por padrão, os tokens expiram em uma hora. O seed cria uma
coordenadora somente quando ainda não existe uma e as duas variáveis
`ICOL_ADMIN_EMAIL` e `ICOL_ADMIN_PASSWORD` foram configuradas.

O banco PostgreSQL precisa existir antes de iniciar. O Flyway aplica as
migrations de `src/main/resources/db/migration/`, e o Hibernate valida o schema
sem alterá-lo.

Inicie o backend na raiz do módulo:

```bash
./mvnw spring-boot:run
```

A API fica em `http://localhost:6060/api`.

## Autenticação e perfis

Faça login com `POST /api/auth/login`:

```json
{
  "email": "coordenadora@icol.org.br",
  "senha": "<senha-configurada-no-seed>"
}
```

Para acessar rotas protegidas, envie o `accessToken` da resposta no cabeçalho
`Authorization` com o esquema `Bearer`. `GET /api/auth/me` retorna o perfil autenticado.
`GET /api/cursos` e `GET /api/turmas` (somente turmas abertas) são públicos.

- **COORDENADORA:** administra usuários, docentes, cursos e turmas; possui
  acesso administrativo a matrículas e frequências.
- **ALUNO:** acessa apenas seu cadastro, suas matrículas e suas frequências.
- **PROFESSOR:** acessa alunos e matrículas das próprias turmas, registra
  frequências nas próprias turmas e administra os próprios avisos. Os dados de
  aluno retornados ao professor não incluem CPF nem declaração socioeconômica.

## Limitações de segurança

- Senhas novas são armazenadas usando Argon2id com salt aleatório. Hashes
  BCrypt antigos sem prefixo não são migrados automaticamente; recrie os
  usuários de teste.
- Argon2id é intencionalmente custoso em CPU e memória. Dimensione os recursos
  usando testes de carga, sem reduzir os parâmetros do hash sem avaliação.
- O limite de tentativas de login é mantido em memória por instância; em
  múltiplas réplicas, cada instância aplica seu próprio limite.
- JWT não possui revogação antes do vencimento. Um token pode continuar válido
  até expirar mesmo depois da inativação da conta; a validade padrão é uma hora.

## Testes

```bash
./mvnw test
```

Os testes de integração iniciam PostgreSQL via Testcontainers e precisam do
Docker disponível.
