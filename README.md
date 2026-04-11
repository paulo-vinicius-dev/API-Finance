# Finance API

API REST para gerenciamento de finanças pessoais. Permite controlar receitas e despesas, organizar transações por categorias e contas, definir orçamentos mensais e acompanhar a saúde financeira com relatórios e insights automáticos.

## Tecnologias

- Java 21
- Spring Boot 4.0.5
- Spring Security (OAuth2 Resource Server / JWT)
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Lombok
- SpringDoc OpenAPI (Swagger)
- Docker

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker e Docker Compose

## Configuração

### 1. Variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
DATABASE_URL=jdbc:postgresql://localhost:5433/finance
DATABASE_NAME=finance
DATABASE_USERNAME=finance
DATABASE_PASSWORD=sua_senha_aqui

# Gere com: openssl rand -base64 32
SECRET_KEY=sua_chave_secreta_base64_aqui

ACCESS_TOKEN_EXPIRY_SECONDS=900      # 15 minutos
REFRESH_TOKEN_EXPIRY_SECONDS=604800  # 7 dias
```

### 2. Subir o banco de dados

```bash
docker compose up -d
```

### 3. Executar a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## Docker (aplicação completa)

Para rodar a aplicação inteira via Docker, crie antes a rede externa:

```bash
docker network create finance-net
```

Build e execução:

```bash
docker build -t finance-api .
docker run --env-file .env --network finance-net -p 8080:8080 finance-api
```

---

## Documentação interativa

Com a aplicação rodando, acesse o Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

## Endpoints

Base URL: `http://localhost:8080`

Endpoints protegidos exigem o header:
```
Authorization: Bearer <access_token>
```

### Autenticação

| Método | Endpoint                  | Descrição                        | Auth |
|--------|---------------------------|----------------------------------|------|
| POST   | `/api/v1/auth/register`   | Criar conta de usuário           | Não  |
| POST   | `/api/v1/auth/login`      | Autenticar e obter tokens        | Não  |
| POST   | `/api/v1/auth/refresh`    | Renovar access token             | Não  |

### Usuários

| Método | Endpoint            | Descrição                        | Auth |
|--------|---------------------|----------------------------------|------|
| GET    | `/api/v1/users/me`  | Dados do usuário autenticado     | Sim  |

### Admin — Gerenciamento de Usuários

> Todos os endpoints abaixo exigem autenticação **e** a role `ADMIN`.

| Método | Endpoint                        | Descrição                                               | Auth  |
|--------|---------------------------------|---------------------------------------------------------|-------|
| GET    | `/api/v1/admin/users`           | Listar todos os usuários (paginado, busca por nome/email) | ADMIN |
| GET    | `/api/v1/admin/users/{id}`      | Buscar usuário por ID                                   | ADMIN |
| PATCH  | `/api/v1/admin/users/{id}`      | Atualizar status ativo e perfis de um usuário           | ADMIN |
| DELETE | `/api/v1/admin/users/{id}`      | Remover permanentemente um usuário                      | ADMIN |

**Parâmetros de listagem (`GET /api/v1/admin/users`):**

| Parâmetro | Tipo   | Padrão     | Descrição                              |
|-----------|--------|------------|----------------------------------------|
| `search`  | string | —          | Filtra por nome ou e-mail (parcial)    |
| `page`    | int    | `0`        | Página (0-based)                       |
| `size`    | int    | `20`       | Itens por página (máx. recomendado: 100) |
| `sort`    | string | `fullName` | Campo de ordenação                     |

**Corpo da requisição PATCH:**
```json
{
  "isActive": true,
  "roles": ["USER"]
}
```

Roles disponíveis: `USER`, `ADMIN`.

**Usuário admin padrão** (criado automaticamente):
```
E-mail: admin@admin.com
Senha:  admin
```

### Contas

| Método | Endpoint                  | Descrição              | Auth |
|--------|---------------------------|------------------------|------|
| GET    | `/api/v1/accounts`        | Listar contas          | Sim  |
| GET    | `/api/v1/accounts/{id}`   | Buscar conta por ID    | Sim  |
| POST   | `/api/v1/accounts`        | Criar conta            | Sim  |
| PUT    | `/api/v1/accounts/{id}`   | Atualizar conta        | Sim  |
| DELETE | `/api/v1/accounts/{id}`   | Remover conta          | Sim  |

### Categorias

| Método | Endpoint                  | Descrição                        | Auth |
|--------|---------------------------|----------------------------------|------|
| GET    | `/api/categories`         | Listar categorias                | Sim  |
| GET    | `/api/categories/{id}`    | Buscar categoria por ID          | Sim  |
| POST   | `/api/categories`         | Criar categoria personalizada    | Sim  |
| PUT    | `/api/categories/{id}`    | Atualizar categoria              | Sim  |
| DELETE | `/api/categories/{id}`    | Remover categoria                | Sim  |

### Transações

| Método | Endpoint                    | Descrição                        | Auth |
|--------|-----------------------------|----------------------------------|------|
| GET    | `/api/transactions`         | Listar transações (com filtros)  | Sim  |
| GET    | `/api/transactions/{id}`    | Buscar transação por ID          | Sim  |
| POST   | `/api/transactions`         | Criar transação                  | Sim  |
| PUT    | `/api/transactions/{id}`    | Atualizar transação              | Sim  |
| DELETE | `/api/transactions/{id}`    | Remover transação                | Sim  |

Filtros disponíveis: `startDate`, `endDate`, `categoryId`, `type` (`INCOME` / `EXPENSE`), `accountId`.

### Orçamentos

| Método | Endpoint                  | Descrição                        | Auth |
|--------|---------------------------|----------------------------------|------|
| GET    | `/api/v1/budgets`         | Listar orçamentos                | Sim  |
| GET    | `/api/v1/budgets/{id}`    | Buscar orçamento por ID          | Sim  |
| POST   | `/api/v1/budgets`         | Criar orçamento                  | Sim  |
| PUT    | `/api/v1/budgets/{id}`    | Atualizar orçamento              | Sim  |
| DELETE | `/api/v1/budgets/{id}`    | Remover orçamento                | Sim  |

### Relatórios

| Método | Endpoint                              | Descrição                          | Auth |
|--------|---------------------------------------|------------------------------------|------|
| GET    | `/api/v1/reports/summary`             | Resumo financeiro do período       | Sim  |
| GET    | `/api/v1/reports/by-category`         | Despesas agrupadas por categoria   | Sim  |
| GET    | `/api/v1/reports/monthly-evolution`   | Evolução mensal de receitas/despesas | Sim  |

### Insights

| Método | Endpoint            | Descrição                              | Auth |
|--------|---------------------|----------------------------------------|------|
| GET    | `/api/v1/insights`  | Análises automáticas do período        | Sim  |

### Alertas

| Método | Endpoint          | Descrição                              | Auth |
|--------|-------------------|----------------------------------------|------|
| GET    | `/api/v1/alerts`  | Alertas financeiros ativos             | Sim  |

### Transações Recorrentes

| Método | Endpoint                    | Descrição                        | Auth |
|--------|-----------------------------|----------------------------------|------|
| GET    | `/api/v1/recurring`         | Listar regras recorrentes        | Sim  |
| GET    | `/api/v1/recurring/{id}`    | Buscar regra por ID              | Sim  |
| POST   | `/api/v1/recurring`         | Criar regra recorrente           | Sim  |
| PUT    | `/api/v1/recurring/{id}`    | Atualizar regra recorrente       | Sim  |
| DELETE | `/api/v1/recurring/{id}`    | Desativar regra recorrente       | Sim  |

---

## Monitoramento

```
GET /actuator/health
```
