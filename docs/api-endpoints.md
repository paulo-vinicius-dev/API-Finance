# Finance API — Documentação de Endpoints

Base URL: `http://localhost:8080`

Todos os endpoints protegidos exigem o header:
```
Authorization: Bearer <access_token>
```

---

## Índice

- [Auth](#auth)
- [Users](#users)
- [Accounts](#accounts)
- [Categories](#categories)
- [Transactions](#transactions)
- [Reports](#reports)
- [Budgets](#budgets)
- [Insights](#insights)
- [Alerts](#alerts)
- [Recurring Transactions](#recurring-transactions)
- [Respostas de Erro](#respostas-de-erro)

---

## Auth

### POST `/api/v1/auth/login`

Autentica o usuário e retorna os tokens de acesso.

**Autenticação:** Não requerida

**Request body:**
```json
{
  "email": "user@email.com",
  "password": "senha123"
}
```

| Campo      | Tipo   | Obrigatório | Descrição         |
|------------|--------|-------------|-------------------|
| `email`    | string | Sim         | E-mail válido     |
| `password` | string | Sim         | Senha do usuário  |

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

---

### POST `/api/v1/auth/register`

Cria uma nova conta de usuário.

**Autenticação:** Não requerida

**Request body:**
```json
{
  "fullName": "João Silva",
  "email": "joao@email.com",
  "password": "Senha@123"
}
```

| Campo      | Tipo   | Obrigatório | Regras                        |
|------------|--------|-------------|-------------------------------|
| `fullName` | string | Sim         | Entre 3 e 100 caracteres      |
| `email`    | string | Sim         | E-mail válido                 |
| `password` | string | Sim         | Entre 6 e 16 caracteres       |

**Response `201 Created`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "roles": ["USER"],
  "isActive": true
}
```

---

### POST `/api/v1/auth/refresh`

Renova o access token usando o refresh token.

**Autenticação:** Não requerida

**Header:**
```
X-Refresh-Token: <refresh_token>
```

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

---

## Users

### GET `/api/v1/users/me`

Retorna os dados do usuário autenticado.

**Autenticação:** Requerida

**Response `200 OK`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "roles": ["USER"],
  "isActive": true
}
```

---

## Accounts

### GET `/api/v1/accounts`

Lista todas as contas do usuário.

**Autenticação:** Requerida

**Response `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Conta Corrente"
  }
]
```

---

### GET `/api/v1/accounts/{id}`

Busca uma conta pelo ID.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição    |
|-----------|------|--------------|
| `id`      | UUID | ID da conta  |

**Response `200 OK`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Conta Corrente"
}
```

**Response `404 Not Found`:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Account not found: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "path": "/api/v1/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "timestamp": "2026-04-05T10:00:00"
}
```

---

### POST `/api/v1/accounts`

Cria uma nova conta para o usuário autenticado.

**Autenticação:** Requerida

**Request body:**
```json
{
  "name": "Conta Corrente"
}
```

| Campo  | Tipo   | Obrigatório | Regras               |
|--------|--------|-------------|----------------------|
| `name` | string | Sim         | Máximo 20 caracteres |

**Response `201 Created`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Conta Corrente"
}
```

---

### PUT `/api/v1/accounts/{id}`

Atualiza uma conta existente.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição   |
|-----------|------|-------------|
| `id`      | UUID | ID da conta |

**Request body:**
```json
{
  "name": "Poupança"
}
```

**Response `200 OK`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Poupança"
}
```

---

### DELETE `/api/v1/accounts/{id}`

Remove uma conta.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição   |
|-----------|------|-------------|
| `id`      | UUID | ID da conta |

**Response `204 No Content`**

---

## Categories

### GET `/api/categories`

Lista as categorias do usuário e as categorias padrão do sistema.

**Autenticação:** Requerida

**Response `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Alimentação",
    "isDefault": true
  }
]
```

---

### GET `/api/categories/{id}`

Busca uma categoria pelo ID (inclui categorias padrão).

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID da categoria |

**Response `200 OK`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Alimentação",
  "isDefault": true
}
```

---

### POST `/api/categories`

Cria uma nova categoria personalizada para o usuário.

**Autenticação:** Requerida

**Request body:**
```json
{
  "name": "Pets"
}
```

| Campo  | Tipo   | Obrigatório | Regras               |
|--------|--------|-------------|----------------------|
| `name` | string | Sim         | Máximo 30 caracteres |

**Response `201 Created`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Pets",
  "isDefault": false
}
```

---

### PUT `/api/categories/{id}`

Atualiza uma categoria personalizada. Categorias padrão não podem ser alteradas.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID da categoria |

**Request body:**
```json
{
  "name": "Animais de Estimação"
}
```

**Response `200 OK`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Animais de Estimação",
  "isDefault": false
}
```

---

### DELETE `/api/categories/{id}`

Remove uma categoria personalizada. Categorias padrão não podem ser removidas.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID da categoria |

**Response `204 No Content`**

---

## Transactions

### GET `/api/transactions`

Lista as transações do usuário com filtros opcionais.

**Autenticação:** Requerida

**Query params (todos opcionais):**

| Parâmetro    | Tipo   | Descrição                                  |
|--------------|--------|--------------------------------------------|
| `startDate`  | date   | Data inicial — formato `YYYY-MM-DD`        |
| `endDate`    | date   | Data final — formato `YYYY-MM-DD`          |
| `categoryId` | UUID   | Filtrar por categoria                      |
| `type`       | enum   | `INCOME` ou `EXPENSE`                      |
| `accountId`  | UUID   | Filtrar por conta                          |

**Exemplos:**
```
GET /api/transactions?startDate=2026-01-01&endDate=2026-01-31
GET /api/transactions?type=EXPENSE&categoryId=b1c2d3e4-...
```

**Response `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "category": {
      "id": "b1c2d3e4-...",
      "name": "Alimentação",
      "isDefault": true
    },
    "account": {
      "id": "a1b2c3d4-...",
      "name": "Conta Corrente"
    },
    "type": "EXPENSE",
    "amount": 49.90,
    "date": "2026-04-05",
    "description": "Almoço"
  }
]
```

---

### GET `/api/transactions/{id}`

Busca uma transação pelo ID.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição        |
|-----------|------|------------------|
| `id`      | UUID | ID da transação  |

**Response `200 OK`:** mesmo formato do item acima.

---

### POST `/api/transactions`

Cria uma nova transação.

**Autenticação:** Requerida

**Request body:**
```json
{
  "categoryId": "b1c2d3e4-5717-4562-b3fc-2c963f66afa6",
  "accountId": "a1b2c3d4-5717-4562-b3fc-2c963f66afa6",
  "type": "EXPENSE",
  "amount": 49.90,
  "date": "2026-04-05",
  "description": "Almoço"
}
```

| Campo         | Tipo    | Obrigatório | Regras                        |
|---------------|---------|-------------|-------------------------------|
| `categoryId`  | UUID    | Sim         | ID de categoria existente     |
| `accountId`   | UUID    | Sim         | ID de conta do usuário        |
| `type`        | enum    | Sim         | `INCOME` ou `EXPENSE`         |
| `amount`      | decimal | Sim         | Valor positivo                |
| `date`        | date    | Sim         | Formato `YYYY-MM-DD`          |
| `description` | string  | Não         | Máximo 100 caracteres         |

**Response `201 Created`:** mesmo formato do `GET /api/transactions/{id}`.

---

### PUT `/api/transactions/{id}`

Atualiza uma transação existente.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID da transação |

**Request body:** mesmo formato do `POST /api/transactions`.

**Response `200 OK`:** mesmo formato do `POST /api/transactions`.

---

### DELETE `/api/transactions/{id}`

Remove uma transação.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID da transação |

**Response `204 No Content`**

---

## Reports

### GET `/api/v1/reports/summary`

Retorna o resumo financeiro do período: total de receitas, despesas, saldo e taxa de economia.

**Autenticação:** Requerida

**Query params (opcionais):**

| Parâmetro   | Tipo | Descrição                           |
|-------------|------|-------------------------------------|
| `startDate` | date | Data inicial — formato `YYYY-MM-DD` |
| `endDate`   | date | Data final — formato `YYYY-MM-DD`   |

**Response `200 OK`:**
```json
{
  "totalIncome": 5000.00,
  "totalExpense": 3200.00,
  "balance": 1800.00,
  "savingsRate": 36.00
}
```

| Campo          | Descrição                                        |
|----------------|--------------------------------------------------|
| `totalIncome`  | Soma de todas as receitas no período             |
| `totalExpense` | Soma de todas as despesas no período             |
| `balance`      | `totalIncome - totalExpense`                     |
| `savingsRate`  | `(balance / totalIncome) * 100` em percentual    |

---

### GET `/api/v1/reports/by-category`

Retorna as despesas agrupadas por categoria com valor total e percentual.

**Autenticação:** Requerida

**Query params (opcionais):**

| Parâmetro   | Tipo | Descrição                           |
|-------------|------|-------------------------------------|
| `startDate` | date | Data inicial — formato `YYYY-MM-DD` |
| `endDate`   | date | Data final — formato `YYYY-MM-DD`   |

**Response `200 OK`:**
```json
[
  {
    "categoryId": "b1c2d3e4-...",
    "categoryName": "Alimentação",
    "total": 800.00,
    "percentage": 25.00
  },
  {
    "categoryId": "c2d3e4f5-...",
    "categoryName": "Transporte",
    "total": 400.00,
    "percentage": 12.50
  }
]
```

---

### GET `/api/v1/reports/monthly-evolution`

Retorna a evolução mensal de receitas e despesas para o ano informado (todos os 12 meses).

**Autenticação:** Requerida

**Query params:**

| Parâmetro | Tipo    | Obrigatório | Descrição                              |
|-----------|---------|-------------|----------------------------------------|
| `year`    | integer | Não         | Ano — padrão: ano atual                |

**Response `200 OK`:**
```json
[
  {
    "month": 1,
    "monthName": "JANUARY",
    "totalIncome": 5000.00,
    "totalExpense": 3200.00,
    "balance": 1800.00
  },
  {
    "month": 2,
    "monthName": "FEBRUARY",
    "totalIncome": 5000.00,
    "totalExpense": 2900.00,
    "balance": 2100.00
  }
]
```

---

## Budgets

### GET `/api/v1/budgets`

Lista os orçamentos do usuário com o valor já gasto e status atual.

**Autenticação:** Requerida

**Query params (opcionais):**

| Parâmetro | Tipo    | Descrição                           |
|-----------|---------|-------------------------------------|
| `month`   | integer | Mês (1–12)                          |
| `year`    | integer | Ano                                 |

**Response `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "category": {
      "id": "b1c2d3e4-...",
      "name": "Alimentação",
      "isDefault": true
    },
    "limitAmount": 800.00,
    "spentAmount": 680.00,
    "usagePercentage": 85.00,
    "status": "WARNING",
    "month": 4,
    "year": 2026
  }
]
```

| Campo             | Descrição                                                  |
|-------------------|------------------------------------------------------------|
| `limitAmount`     | Limite de gasto definido para a categoria no período       |
| `spentAmount`     | Total já gasto na categoria no período                     |
| `usagePercentage` | `(spentAmount / limitAmount) * 100`                        |
| `status`          | `ON_TRACK` (< 80%) · `WARNING` (≥ 80%) · `EXCEEDED` (≥ 100%) |

---

### GET `/api/v1/budgets/{id}`

Busca um orçamento pelo ID.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição        |
|-----------|------|------------------|
| `id`      | UUID | ID do orçamento  |

**Response `200 OK`:** mesmo formato do item acima (objeto único).

---

### POST `/api/v1/budgets`

Cria um orçamento para uma categoria em um período específico.

**Autenticação:** Requerida

**Request body:**
```json
{
  "categoryId": "b1c2d3e4-5717-4562-b3fc-2c963f66afa6",
  "limitAmount": 800.00,
  "month": 4,
  "year": 2026
}
```

| Campo         | Tipo    | Obrigatório | Regras                        |
|---------------|---------|-------------|-------------------------------|
| `categoryId`  | UUID    | Sim         | ID de categoria existente     |
| `limitAmount` | decimal | Sim         | Valor positivo (> 0)          |
| `month`       | integer | Sim         | Entre 1 e 12                  |
| `year`        | integer | Sim         | A partir de 2000              |

**Response `201 Created`:** mesmo formato do `GET /api/v1/budgets/{id}`.

**Response `409 Conflict`:** quando já existe orçamento para a mesma categoria/mês/ano.

---

### PUT `/api/v1/budgets/{id}`

Atualiza um orçamento existente.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID do orçamento |

**Request body:** mesmo formato do `POST /api/v1/budgets`.

**Response `200 OK`:** mesmo formato do `GET /api/v1/budgets/{id}`.

---

### DELETE `/api/v1/budgets/{id}`

Remove um orçamento.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição       |
|-----------|------|-----------------|
| `id`      | UUID | ID do orçamento |

**Response `204 No Content`**

---

## Insights

### GET `/api/v1/insights`

Retorna análises automáticas sobre o comportamento financeiro do período.

**Autenticação:** Requerida

**Query params (opcionais):**

| Parâmetro | Tipo    | Descrição                              |
|-----------|---------|----------------------------------------|
| `month`   | integer | Mês (1–12) — padrão: mês atual         |
| `year`    | integer | Ano — padrão: ano atual                |

**Response `200 OK`:**
```json
[
  {
    "type": "TOP_EXPENSE_CATEGORY",
    "message": "Categoria com maior gasto: Alimentação",
    "value": "R$ 800.00"
  },
  {
    "type": "EXPENSE_INCREASE",
    "message": "Despesas aumentaram 12.5% em relação ao mês anterior",
    "value": "12.5%"
  },
  {
    "type": "SAVINGS_RATE",
    "message": "Taxa de economia do mês: 36.0%",
    "value": "36.0%"
  },
  {
    "type": "BUDGET_WARNING",
    "message": "Orçamento de Alimentação próximo do limite (85.0% utilizado)",
    "value": "85.0%"
  }
]
```

| Tipo (`type`)               | Descrição                                              |
|-----------------------------|--------------------------------------------------------|
| `TOP_EXPENSE_CATEGORY`      | Categoria com maior gasto no período                   |
| `MONTHLY_COMPARISON`        | Despesas reduziram vs. mês anterior                    |
| `EXPENSE_INCREASE`          | Despesas aumentaram vs. mês anterior                   |
| `SAVINGS_RATE`              | Taxa de economia do mês                                |
| `BUDGET_WARNING`            | Orçamento próximo do limite (≥ 80%)                    |
| `BUDGET_EXCEEDED`           | Orçamento ultrapassado (≥ 100%)                        |

---

## Alerts

### GET `/api/v1/alerts`

Retorna alertas financeiros ativos para o período. Alertas são gerados a partir de orçamentos e variações de gastos.

**Autenticação:** Requerida

**Query params (opcionais):**

| Parâmetro | Tipo    | Descrição                              |
|-----------|---------|----------------------------------------|
| `month`   | integer | Mês (1–12) — padrão: mês atual         |
| `year`    | integer | Ano — padrão: ano atual                |

**Response `200 OK`:**
```json
[
  {
    "type": "BUDGET_EXCEEDED",
    "severity": "DANGER",
    "message": "Orçamento de Alimentação ultrapassado. Gasto: R$ 900.00 / Limite: R$ 800.00",
    "categoryId": "b1c2d3e4-...",
    "categoryName": "Alimentação"
  },
  {
    "type": "SIGNIFICANT_EXPENSE_INCREASE",
    "severity": "WARNING",
    "message": "Gastos com Lazer aumentaram 45.0% em relação ao mês anterior",
    "categoryId": "c2d3e4f5-...",
    "categoryName": "Lazer"
  }
]
```

| Tipo (`type`)                  | Severidade  | Gatilho                                           |
|--------------------------------|-------------|---------------------------------------------------|
| `BUDGET_EXCEEDED`              | `DANGER`    | Gasto ≥ 100% do limite do orçamento               |
| `BUDGET_WARNING`               | `WARNING`   | Gasto ≥ 80% do limite do orçamento                |
| `SIGNIFICANT_EXPENSE_INCREASE` | `WARNING`   | Gasto na categoria > 30% maior que o mês anterior |

---

## Recurring Transactions

### GET `/api/v1/recurring`

Lista todas as regras de movimentações recorrentes do usuário.

**Autenticação:** Requerida

**Response `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "category": {
      "id": "b1c2d3e4-...",
      "name": "Moradia",
      "isDefault": true
    },
    "account": {
      "id": "a1b2c3d4-...",
      "name": "Conta Corrente"
    },
    "type": "EXPENSE",
    "amount": 1500.00,
    "description": "Aluguel",
    "frequency": "MONTHLY",
    "startDate": "2026-01-01",
    "nextDueDate": "2026-05-01",
    "isActive": true
  }
]
```

---

### GET `/api/v1/recurring/{id}`

Busca uma regra recorrente pelo ID.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição               |
|-----------|------|-------------------------|
| `id`      | UUID | ID da regra recorrente  |

**Response `200 OK`:** mesmo formato do item acima (objeto único).

---

### POST `/api/v1/recurring`

Cria uma nova regra de movimentação recorrente.

**Autenticação:** Requerida

**Request body:**
```json
{
  "categoryId": "b1c2d3e4-5717-4562-b3fc-2c963f66afa6",
  "accountId": "a1b2c3d4-5717-4562-b3fc-2c963f66afa6",
  "type": "EXPENSE",
  "amount": 1500.00,
  "description": "Aluguel",
  "frequency": "MONTHLY",
  "startDate": "2026-05-01"
}
```

| Campo         | Tipo    | Obrigatório | Regras                              |
|---------------|---------|-------------|-------------------------------------|
| `categoryId`  | UUID    | Sim         | ID de categoria existente           |
| `accountId`   | UUID    | Sim         | ID de conta do usuário              |
| `type`        | enum    | Sim         | `INCOME` ou `EXPENSE`               |
| `amount`      | decimal | Sim         | Valor positivo (> 0)                |
| `description` | string  | Não         | Máximo 100 caracteres               |
| `frequency`   | enum    | Sim         | `DAILY` · `WEEKLY` · `MONTHLY` · `YEARLY` |
| `startDate`   | date    | Sim         | Formato `YYYY-MM-DD`                |

**Response `201 Created`:** mesmo formato do `GET /api/v1/recurring/{id}`.

---

### PUT `/api/v1/recurring/{id}`

Atualiza uma regra recorrente. O `nextDueDate` é reiniciado para o novo `startDate`.

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição               |
|-----------|------|-------------------------|
| `id`      | UUID | ID da regra recorrente  |

**Request body:** mesmo formato do `POST /api/v1/recurring`.

**Response `200 OK`:** mesmo formato do `GET /api/v1/recurring/{id}`.

---

### DELETE `/api/v1/recurring/{id}`

Desativa uma regra recorrente (soft delete — a regra não será mais processada pelo scheduler).

**Autenticação:** Requerida

**Path params:**

| Parâmetro | Tipo | Descrição               |
|-----------|------|-------------------------|
| `id`      | UUID | ID da regra recorrente  |

**Response `204 No Content`**

---

## Respostas de Erro

Todos os erros seguem o formato padronizado:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Account not found: 3fa85f64-...",
  "path": "/api/v1/accounts/3fa85f64-...",
  "timestamp": "2026-04-05T10:00:00"
}
```

Erros de validação incluem o campo `fieldErrors`:

```json
{
  "status": 422,
  "error": "Validation Error",
  "message": "Erro de validação nos campos informados",
  "path": "/api/v1/budgets",
  "timestamp": "2026-04-05T10:00:00",
  "fieldErrors": [
    {
      "field": "limitAmount",
      "message": "Valor limite deve ser maior que zero"
    }
  ]
}
```

### Códigos de status

| Status | Descrição                              |
|--------|----------------------------------------|
| `200`  | OK — requisição bem-sucedida           |
| `201`  | Created — recurso criado               |
| `204`  | No Content — recurso removido          |
| `400`  | Bad Request — parâmetro inválido       |
| `401`  | Unauthorized — token ausente/inválido  |
| `403`  | Forbidden — sem permissão              |
| `404`  | Not Found — recurso não encontrado     |
| `409`  | Conflict — recurso já existe           |
| `422`  | Unprocessable Entity — falha validação |
| `500`  | Internal Server Error — erro interno   |
