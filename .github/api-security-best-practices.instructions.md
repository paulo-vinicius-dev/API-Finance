# Melhores Práticas de Segurança para APIs

> **Propósito:** Referência para agentes de IA gerarem código seguro por padrão em APIs REST (FastAPI/Python). Cada seção contém regras obrigatórias, justificativa e código de implementação.

---

## Índice

1. [Autenticação](#1-autenticação)
2. [Autorização e Controle de Acesso](#2-autorização-e-controle-de-acesso)
3. [Validação e Sanitização de Entrada](#3-validação-e-sanitização-de-entrada)
4. [Rate Limiting e Proteção contra Abuso](#4-rate-limiting-e-proteção-contra-abuso)
5. [CORS — Cross-Origin Resource Sharing](#5-cors--cross-origin-resource-sharing)
6. [Headers de Segurança](#6-headers-de-segurança)
7. [Proteção de Dados Sensíveis](#7-proteção-de-dados-sensíveis)
8. [Logging e Auditoria](#8-logging-e-auditoria)
9. [Segurança no Banco de Dados](#9-segurança-no-banco-de-dados)
10. [Proteção contra Ataques Comuns](#10-proteção-contra-ataques-comuns)
11. [Gerenciamento de Dependências](#11-gerenciamento-de-dependências)
12. [Segurança em Uploads de Arquivos](#12-segurança-em-uploads-de-arquivos)
13. [Tratamento Seguro de Erros](#13-tratamento-seguro-de-erros)
14. [HTTPS e Comunicação Segura](#14-https-e-comunicação-segura)
15. [Configuração Segura do Ambiente](#15-configuração-segura-do-ambiente)
16. [Checklist de Segurança para o Agente de IA](#16-checklist-de-segurança-para-o-agente-de-ia)

---

## 1. Autenticação

### 1.1 Regras Obrigatórias

- Usar **JWT (JSON Web Tokens)** com tempo de expiração curto para access tokens.
- Implementar **refresh tokens** com rotação obrigatória (o refresh token usado é invalidado ao gerar um novo par).
- Senhas armazenadas **exclusivamente** com hashing forte — nunca em texto plano, nunca com MD5/SHA1.
- Usar **bcrypt** ou **argon2** para hashing de senhas.
- Access token: expiração de **15–30 minutos**. Refresh token: expiração de **7–30 dias**.
- Nunca incluir dados sensíveis no payload do JWT (senha, CPF, dados de cartão).
- Validar **todas** as claims do JWT: `exp`, `iss`, `aud`, `sub`.

### 1.2 Configuração do JWT

```python
# app/core/security.py
from datetime import datetime, timedelta, timezone

from jose import JWTError, jwt
from passlib.context import CryptContext

from app.config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

ALGORITHM = "HS256"


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)


def create_access_token(subject: str, extra_claims: dict | None = None) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": subject,
        "iss": settings.APP_NAME,
        "aud": settings.APP_NAME,
        "iat": now,
        "exp": now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
        "type": "access",
    }
    if extra_claims:
        payload.update(extra_claims)
    return jwt.encode(payload, settings.SECRET_KEY, algorithm=ALGORITHM)


def create_refresh_token(subject: str) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": subject,
        "iss": settings.APP_NAME,
        "aud": settings.APP_NAME,
        "iat": now,
        "exp": now + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
        "type": "refresh",
    }
    return jwt.encode(payload, settings.REFRESH_SECRET_KEY, algorithm=ALGORITHM)


def decode_token(token: str, *, token_type: str = "access") -> dict:
    """Decodifica e valida o token. Lança JWTError se inválido."""
    secret = (
        settings.SECRET_KEY
        if token_type == "access"
        else settings.REFRESH_SECRET_KEY
    )
    payload = jwt.decode(
        token,
        secret,
        algorithms=[ALGORITHM],
        audience=settings.APP_NAME,
        issuer=settings.APP_NAME,
    )
    if payload.get("type") != token_type:
        raise JWTError("Tipo de token inválido")
    return payload
```

### 1.3 Dependência de Autenticação

```python
# app/dependencies.py
import uuid
from typing import Annotated

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import decode_token
from app.database import AsyncSessionLocal
from app.modules.user.model import User

bearer_scheme = HTTPBearer(auto_error=True)


async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise


async def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials, Depends(bearer_scheme)],
    session: Annotated[AsyncSession, Depends(get_db)],
) -> User:
    token = credentials.credentials

    try:
        payload = decode_token(token, token_type="access")
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido ou expirado",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user = await session.get(User, uuid.UUID(payload["sub"]))

    if not user or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuário não encontrado ou inativo",
        )
    return user


# Alias para type hints limpos nos controllers
CurrentUser = Annotated[User, Depends(get_current_user)]
```

### 1.4 Endpoint de Login — Proteção contra Timing Attack

```python
# app/modules/auth/controller.py
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import (
    create_access_token,
    create_refresh_token,
    hash_password,
    verify_password,
)
from app.dependencies import get_db
from app.modules.auth.schema import LoginRequest, TokenResponse
from app.modules.user.repository import UserRepository

router = APIRouter(prefix="/auth", tags=["Auth"])

# Hash "fantasma" para manter tempo de resposta constante
# quando o email não existe (evita user enumeration)
DUMMY_HASH = hash_password("dummy-password-placeholder")


@router.post("/login", response_model=TokenResponse)
async def login(
    data: LoginRequest,
    session: AsyncSession = Depends(get_db),
):
    repo = UserRepository(session)
    user = await repo.find_by_email(data.email)

    # Sempre executa verify_password para evitar timing attack
    hashed = user.hashed_password if user else DUMMY_HASH
    password_valid = verify_password(data.password, hashed)

    if not user or not password_valid or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciais inválidas",
        )

    return TokenResponse(
        access_token=create_access_token(str(user.id)),
        refresh_token=create_refresh_token(str(user.id)),
        token_type="bearer",
    )
```

---

## 2. Autorização e Controle de Acesso

### 2.1 Regras Obrigatórias

- Aplicar **princípio do menor privilégio** — conceder apenas as permissões necessárias.
- Implementar **RBAC (Role-Based Access Control)** ou **ABAC (Attribute-Based)** conforme complexidade.
- Verificar autorização em **toda** rota protegida, sem exceção.
- Nunca confiar no client-side para controle de acesso.
- Proteger contra **IDOR (Insecure Direct Object Reference)**: verificar se o usuário tem permissão sobre o recurso específico, não apenas se está autenticado.

### 2.2 Sistema de Permissões (RBAC)

```python
# app/core/permissions.py
from enum import StrEnum
from functools import wraps
from typing import Callable

from fastapi import HTTPException, status

from app.modules.user.model import User


class Role(StrEnum):
    ADMIN = "admin"
    MANAGER = "manager"
    USER = "user"


# Mapa de hierarquia: cada role inclui suas permissões + as abaixo
ROLE_HIERARCHY: dict[Role, set[str]] = {
    Role.USER: {
        "profile:read",
        "profile:update",
        "orders:read_own",
        "orders:create",
    },
    Role.MANAGER: {
        "users:read",
        "orders:read_all",
        "orders:update",
        "reports:read",
    },
    Role.ADMIN: {
        "users:read",
        "users:create",
        "users:update",
        "users:delete",
        "orders:read_all",
        "orders:update",
        "orders:delete",
        "reports:read",
        "reports:export",
        "settings:manage",
    },
}


def _get_permissions(role: Role) -> set[str]:
    """Retorna permissões acumuladas com a hierarquia."""
    base = ROLE_HIERARCHY.get(Role.USER, set())
    if role == Role.USER:
        return base
    return base | ROLE_HIERARCHY.get(role, set())


def require_permissions(*permissions: str) -> Callable:
    """Dependência que exige permissões específicas."""

    def dependency(current_user: User) -> User:
        user_permissions = _get_permissions(Role(current_user.role))
        missing = set(permissions) - user_permissions

        if missing:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Permissão insuficiente",
            )
        return current_user

    return dependency


def require_role(*roles: Role) -> Callable:
    """Dependência que exige uma role específica."""

    def dependency(current_user: User) -> User:
        if Role(current_user.role) not in roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Acesso negado para esta role",
            )
        return current_user

    return dependency
```

### 2.3 Uso no Controller

```python
# Exemplo: endpoint restrito a admin
from app.core.permissions import require_permissions, require_role, Role

@router.get("/admin/users", response_model=list[UserResponse])
async def list_all_users(
    current_user: CurrentUser,
    _: User = Depends(require_role(Role.ADMIN, Role.MANAGER)),
    service: UserService = Depends(get_user_service),
):
    return await service.list_users()


# Exemplo: proteção contra IDOR
@router.get("/orders/{order_id}", response_model=OrderResponse)
async def get_order(
    order_id: uuid.UUID,
    current_user: CurrentUser,
    service: OrderService = Depends(get_order_service),
):
    order = await service.get_order(order_id)

    # IDOR check: usuário só acessa seus próprios pedidos
    if current_user.role == "user" and order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Acesso negado")

    return order
```

---

## 3. Validação e Sanitização de Entrada

### 3.1 Regras Obrigatórias

- **Toda** entrada do usuário deve ser validada via schemas Pydantic — nunca confiar no que vem do client.
- Definir **limites explícitos**: `min_length`, `max_length`, `ge`, `le`, `pattern`.
- Sanitizar strings contra **XSS** antes de armazenar.
- Validar **formato, tipo e domínio** de cada campo.
- Rejeitar payloads que excedam um **tamanho máximo** (body size limit).
- Validar e limitar parâmetros de **paginação** (offset/limit) para evitar queries abusivas.
- Nunca usar entrada do usuário diretamente em queries SQL, nomes de arquivo, comandos shell ou templates.

### 3.2 Schemas com Validação Forte

```python
# app/modules/user/schema.py
import re
import uuid
from datetime import datetime

from pydantic import (
    BaseModel,
    ConfigDict,
    EmailStr,
    Field,
    field_validator,
)


class UserCreate(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8, max_length=128)
    full_name: str = Field(min_length=1, max_length=150)

    @field_validator("password")
    @classmethod
    def validate_password_strength(cls, v: str) -> str:
        if not re.search(r"[A-Z]", v):
            raise ValueError("Senha deve conter pelo menos uma letra maiúscula")
        if not re.search(r"[a-z]", v):
            raise ValueError("Senha deve conter pelo menos uma letra minúscula")
        if not re.search(r"\d", v):
            raise ValueError("Senha deve conter pelo menos um número")
        if not re.search(r"[!@#$%^&*(),.?\":{}|<>]", v):
            raise ValueError("Senha deve conter pelo menos um caractere especial")
        return v

    @field_validator("full_name")
    @classmethod
    def sanitize_name(cls, v: str) -> str:
        # Remove tags HTML/scripts
        clean = re.sub(r"<[^>]*>", "", v)
        # Remove caracteres de controle
        clean = re.sub(r"[\x00-\x1f\x7f-\x9f]", "", clean)
        return clean.strip()


class PaginationParams(BaseModel):
    """Parâmetros de paginação validados e limitados."""
    page: int = Field(default=1, ge=1, le=10000)
    per_page: int = Field(default=20, ge=1, le=100)

    @property
    def offset(self) -> int:
        return (self.page - 1) * self.per_page


class SearchParams(BaseModel):
    """Parâmetros de busca com sanitização."""
    q: str = Field(default="", max_length=200)

    @field_validator("q")
    @classmethod
    def sanitize_search(cls, v: str) -> str:
        # Remove caracteres perigosos para LIKE queries
        return re.sub(r"[%_\\]", "", v).strip()
```

### 3.3 Limite de Body Size

```python
# app/core/middleware.py
from fastapi import Request, HTTPException, status
from starlette.middleware.base import BaseHTTPMiddleware


class RequestSizeLimitMiddleware(BaseHTTPMiddleware):
    """Rejeita requisições com body acima do limite permitido."""

    def __init__(self, app, max_size_bytes: int = 1_048_576):  # 1 MB
        super().__init__(app)
        self.max_size_bytes = max_size_bytes

    async def dispatch(self, request: Request, call_next):
        content_length = request.headers.get("content-length")

        if content_length and int(content_length) > self.max_size_bytes:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail=f"Payload excede o limite de {self.max_size_bytes} bytes",
            )

        return await call_next(request)
```

---

## 4. Rate Limiting e Proteção contra Abuso

### 4.1 Regras Obrigatórias

- Implementar **rate limiting** em todos os endpoints públicos.
- Aplicar limites **mais restritivos** em rotas de autenticação (login, registro, reset de senha).
- Usar **slowapi** ou implementação customizada com Redis.
- Identificar clientes por **IP + token** (quando autenticado).
- Retornar headers `X-RateLimit-*` e status `429 Too Many Requests`.
- Implementar **backoff exponencial** em tentativas de login falhas (account lockout temporário).

### 4.2 Implementação com slowapi

```python
# app/core/rate_limit.py
from slowapi import Limiter
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from fastapi import Request
from fastapi.responses import JSONResponse


def _key_func(request: Request) -> str:
    """Identifica o cliente por IP. Se autenticado, usa user ID."""
    auth_header = request.headers.get("authorization", "")
    if auth_header.startswith("Bearer "):
        # Extrair sub do token sem validar (apenas para key)
        # Em produção, considerar usar um hash do token
        return f"user:{auth_header[-16:]}"
    return get_remote_address(request)


limiter = Limiter(
    key_func=_key_func,
    default_limits=["200/minute"],
    storage_uri="memory://",  # Usar "redis://localhost:6379" em produção
)


async def rate_limit_exceeded_handler(
    request: Request, exc: RateLimitExceeded
) -> JSONResponse:
    return JSONResponse(
        status_code=429,
        content={
            "detail": "Muitas requisições. Tente novamente mais tarde.",
            "retry_after": exc.detail,
        },
        headers={
            "Retry-After": str(exc.detail),
            "X-RateLimit-Limit": request.state.view_rate_limit or "",
        },
    )
```

### 4.3 Aplicação nas Rotas

```python
# Rotas de autenticação — limites mais restritivos
@router.post("/login")
@limiter.limit("5/minute")  # Máximo 5 tentativas de login por minuto
async def login(request: Request, data: LoginRequest, ...):
    ...


@router.post("/register")
@limiter.limit("3/minute")
async def register(request: Request, data: UserCreate, ...):
    ...


@router.post("/forgot-password")
@limiter.limit("3/hour")  # Reset de senha ainda mais restrito
async def forgot_password(request: Request, data: ForgotPasswordRequest, ...):
    ...


# Rotas gerais
@router.get("/products")
@limiter.limit("60/minute")
async def list_products(request: Request, ...):
    ...
```

### 4.4 Account Lockout Temporário

```python
# app/core/login_protection.py
from datetime import datetime, timedelta, timezone
from collections import defaultdict


class LoginProtection:
    """Bloqueio temporário após tentativas falhas consecutivas."""

    def __init__(
        self,
        max_attempts: int = 5,
        lockout_minutes: int = 15,
    ):
        self.max_attempts = max_attempts
        self.lockout_duration = timedelta(minutes=lockout_minutes)
        self._attempts: dict[str, list[datetime]] = defaultdict(list)
        self._lockouts: dict[str, datetime] = {}

    def is_locked(self, identifier: str) -> bool:
        lockout_until = self._lockouts.get(identifier)
        if lockout_until and datetime.now(timezone.utc) < lockout_until:
            return True
        if lockout_until:
            del self._lockouts[identifier]
        return False

    def record_failure(self, identifier: str) -> None:
        now = datetime.now(timezone.utc)
        cutoff = now - self.lockout_duration
        # Mantém apenas tentativas recentes
        self._attempts[identifier] = [
            t for t in self._attempts[identifier] if t > cutoff
        ]
        self._attempts[identifier].append(now)

        if len(self._attempts[identifier]) >= self.max_attempts:
            self._lockouts[identifier] = now + self.lockout_duration
            self._attempts[identifier].clear()

    def record_success(self, identifier: str) -> None:
        self._attempts.pop(identifier, None)
        self._lockouts.pop(identifier, None)


# Singleton — em produção, usar Redis
login_protection = LoginProtection()
```

---

## 5. CORS — Cross-Origin Resource Sharing

### 5.1 Regras Obrigatórias

- **Nunca** usar `allow_origins=["*"]` em produção.
- Listar **explicitamente** as origens permitidas.
- Restringir `allow_methods` apenas aos métodos usados.
- Restringir `allow_headers` apenas aos headers necessários.
- Desabilitar `allow_credentials` se não precisar enviar cookies cross-origin.

### 5.2 Implementação

```python
# app/main.py
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings


def configure_cors(app: FastAPI) -> None:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.ALLOWED_ORIGINS,  # ["https://app.example.com"]
        allow_credentials=True,
        allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE"],
        allow_headers=[
            "Authorization",
            "Content-Type",
            "X-Request-ID",
        ],
        expose_headers=["X-Request-ID", "X-RateLimit-Remaining"],
        max_age=600,  # Cache de preflight por 10 minutos
    )
```

```python
# app/config.py (trecho)
class Settings(BaseSettings):
    ALLOWED_ORIGINS: list[str] = ["http://localhost:3000"]  # Override via env
```

```env
# .env — Produção
ALLOWED_ORIGINS=["https://app.meusite.com","https://admin.meusite.com"]
```

---

## 6. Headers de Segurança

### 6.1 Regras Obrigatórias

- Adicionar headers de segurança em **todas** as respostas.
- `X-Content-Type-Options: nosniff` — impede MIME sniffing.
- `X-Frame-Options: DENY` — impede clickjacking.
- `Strict-Transport-Security` — força HTTPS.
- `Content-Security-Policy` — controla fontes de conteúdo.
- Remover headers que expõem informações do servidor (`Server`, `X-Powered-By`).

### 6.2 Middleware de Headers

```python
# app/core/middleware.py
from starlette.middleware.base import BaseHTTPMiddleware
from fastapi import Request


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        response = await call_next(request)

        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
        response.headers["Permissions-Policy"] = (
            "camera=(), microphone=(), geolocation=()"
        )
        response.headers["Strict-Transport-Security"] = (
            "max-age=63072000; includeSubDomains; preload"
        )
        response.headers["Content-Security-Policy"] = (
            "default-src 'self'; frame-ancestors 'none'"
        )

        # Remover headers que expõem info do servidor
        response.headers.pop("Server", None)
        response.headers.pop("X-Powered-By", None)

        return response
```

---

## 7. Proteção de Dados Sensíveis

### 7.1 Regras Obrigatórias

- **Nunca** retornar senhas, tokens, chaves ou dados de cartão na resposta da API.
- **Nunca** logar dados sensíveis (senhas, tokens, corpos de requisição com dados pessoais).
- Mascarar dados parcialmente quando exibição for necessária (ex.: `****1234`).
- Usar `SecretStr` do Pydantic para campos sensíveis em configuração.
- Criptografar dados sensíveis em repouso (at rest) quando necessário.
- Schemas de resposta devem **excluir explicitamente** campos sensíveis.

### 7.2 Schema com Proteção

```python
# app/modules/user/schema.py
from pydantic import BaseModel, ConfigDict, EmailStr, Field


class UserResponse(BaseModel):
    """Resposta pública — nunca expor hashed_password ou tokens."""
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    email: EmailStr
    full_name: str
    is_active: bool
    created_at: datetime

    # Campos sensíveis NÃO estão aqui:
    # - hashed_password
    # - refresh_token
    # - reset_token
    # - phone (dependendo da política)
```

### 7.3 Configuração Segura

```python
# app/config.py
from pydantic import SecretStr
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    SECRET_KEY: SecretStr
    REFRESH_SECRET_KEY: SecretStr
    DATABASE_URL: SecretStr

    def get_database_url(self) -> str:
        return self.DATABASE_URL.get_secret_value()
```

### 7.4 Mascaramento de Dados

```python
# app/common/masking.py

def mask_email(email: str) -> str:
    """usuario@dominio.com → u******o@dominio.com"""
    local, domain = email.split("@")
    if len(local) <= 2:
        masked = local[0] + "***"
    else:
        masked = local[0] + "*" * (len(local) - 2) + local[-1]
    return f"{masked}@{domain}"


def mask_cpf(cpf: str) -> str:
    """12345678901 → ***.***.***-01"""
    clean = cpf.replace(".", "").replace("-", "")
    return f"***.***.**{clean[-3:-2]}-{clean[-2:]}"


def mask_card_number(number: str) -> str:
    """4111111111111111 → ****-****-****-1111"""
    clean = number.replace("-", "").replace(" ", "")
    return f"****-****-****-{clean[-4:]}"
```

---

## 8. Logging e Auditoria

### 8.1 Regras Obrigatórias

- Logar **todas** as ações sensíveis: login, logout, alteração de senha, alteração de role, deleção de dados.
- **Nunca** logar senhas, tokens, corpos de requisição com dados pessoais completos.
- Usar **structured logging** (JSON) para facilitar análise.
- Incluir **request ID** em todos os logs para rastreabilidade.
- Logar **IP, user-agent, user_id e timestamp** em eventos de segurança.
- Definir níveis de log adequados: `INFO` para operações normais, `WARNING` para suspeitas, `ERROR` para falhas.

### 8.2 Configuração de Logging

```python
# app/core/logging.py
import logging
import sys
import uuid
from contextvars import ContextVar

from pythonjsonlogger import jsonlogger

request_id_ctx: ContextVar[str] = ContextVar("request_id", default="")


class RequestIdFilter(logging.Filter):
    def filter(self, record):
        record.request_id = request_id_ctx.get("")
        return True


def setup_logging(level: str = "INFO") -> logging.Logger:
    logger = logging.getLogger("app")
    logger.setLevel(getattr(logging, level.upper()))

    handler = logging.StreamHandler(sys.stdout)
    formatter = jsonlogger.JsonFormatter(
        fmt="%(asctime)s %(levelname)s %(name)s %(request_id)s %(message)s",
        datefmt="%Y-%m-%dT%H:%M:%S%z",
    )
    handler.setFormatter(formatter)
    handler.addFilter(RequestIdFilter())

    logger.addHandler(handler)
    return logger


logger = setup_logging()
```

### 8.3 Middleware de Request ID

```python
# app/core/middleware.py
import uuid
from app.core.logging import request_id_ctx


class RequestIdMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        rid = request.headers.get("X-Request-ID", str(uuid.uuid4()))
        request_id_ctx.set(rid)

        response = await call_next(request)
        response.headers["X-Request-ID"] = rid
        return response
```

### 8.4 Auditoria de Eventos de Segurança

```python
# app/core/audit.py
from enum import StrEnum
from app.core.logging import logger


class AuditEvent(StrEnum):
    LOGIN_SUCCESS = "login_success"
    LOGIN_FAILURE = "login_failure"
    LOGOUT = "logout"
    PASSWORD_CHANGE = "password_change"
    ROLE_CHANGE = "role_change"
    ACCOUNT_LOCKED = "account_locked"
    DATA_EXPORT = "data_export"
    USER_DELETED = "user_deleted"
    PERMISSION_DENIED = "permission_denied"


def audit_log(
    event: AuditEvent,
    *,
    user_id: str | None = None,
    ip_address: str | None = None,
    user_agent: str | None = None,
    details: dict | None = None,
) -> None:
    logger.info(
        "AUDIT",
        extra={
            "audit_event": event.value,
            "user_id": user_id,
            "ip_address": ip_address,
            "user_agent": user_agent,
            "details": details or {},
        },
    )
```

```python
# Uso no login
from app.core.audit import audit_log, AuditEvent

audit_log(
    AuditEvent.LOGIN_FAILURE,
    ip_address=request.client.host,
    user_agent=request.headers.get("user-agent"),
    details={"email": mask_email(data.email), "reason": "invalid_password"},
)
```

---

## 9. Segurança no Banco de Dados

### 9.1 Regras Obrigatórias

- **Sempre** usar ORM (SQLAlchemy) ou query builder parametrizado — nunca concatenar strings SQL.
- Quando SQL raw for inevitável, usar **`text()` com `bindparams`**.
- Credenciais do banco via **variáveis de ambiente**, nunca hardcoded.
- Usuário do banco com **permissões mínimas** (não usar root/superuser para a aplicação).
- Habilitar **SSL/TLS** na conexão com o banco em produção.
- Definir **pool de conexões** com limites adequados.

### 9.2 Query Parametrizada (Segura)

```python
# CORRETO — parametrizado via ORM
stmt = select(User).where(User.email == email)

# CORRETO — SQL raw parametrizado
from sqlalchemy import text
stmt = text("SELECT * FROM users WHERE email = :email")
result = await session.execute(stmt, {"email": email})

# ERRADO — NUNCA fazer isso (SQL Injection)
# stmt = text(f"SELECT * FROM users WHERE email = '{email}'")
```

### 9.3 Conexão Segura

```python
# app/database.py
from sqlalchemy.ext.asyncio import create_async_engine
from app.config import settings

engine = create_async_engine(
    settings.get_database_url(),
    echo=False,  # Nunca True em produção (loga queries)
    pool_size=20,
    max_overflow=10,
    pool_timeout=30,
    pool_recycle=1800,  # Recicla conexões a cada 30 min
    pool_pre_ping=True,  # Testa conexão antes de usar
    connect_args={
        "ssl": "require",  # PostgreSQL — exigir SSL
    },
)
```

---

## 10. Proteção contra Ataques Comuns

### 10.1 SQL Injection

**Prevenção:** Usar ORM (SQLAlchemy) com queries parametrizadas. Nunca interpolar variáveis em strings SQL.

```python
# SEGURO
stmt = select(Product).where(Product.name.ilike(f"%{sanitized_search}%"))

# INSEGURO — vulnerável a SQL Injection
# query = f"SELECT * FROM products WHERE name LIKE '%{user_input}%'"
```

### 10.2 XSS (Cross-Site Scripting)

**Prevenção:** Sanitizar toda entrada do usuário antes de armazenar. A API retorna JSON (já escapa HTML), mas dados podem ser renderizados em frontends.

```python
# app/common/sanitize.py
import re
import html


def sanitize_html(value: str) -> str:
    """Remove todas as tags HTML e escapa entidades."""
    clean = re.sub(r"<[^>]*>", "", value)
    return html.escape(clean)


def sanitize_for_storage(value: str) -> str:
    """Sanitização para armazenamento seguro."""
    # Remove null bytes
    value = value.replace("\x00", "")
    # Remove caracteres de controle
    value = re.sub(r"[\x01-\x08\x0b\x0c\x0e-\x1f\x7f]", "", value)
    return value.strip()
```

### 10.3 CSRF (Cross-Site Request Forgery)

**Prevenção:** APIs stateless com JWT no header `Authorization` são naturalmente protegidas contra CSRF (o browser não envia automaticamente). Se usar cookies para autenticação, implementar token CSRF.

```python
# Se usar cookies (SameSite é a primeira defesa)
response.set_cookie(
    key="access_token",
    value=token,
    httponly=True,      # Inacessível via JavaScript
    secure=True,        # Apenas HTTPS
    samesite="strict",  # Não enviado cross-origin
    max_age=1800,
)
```

### 10.4 Mass Assignment

**Prevenção:** Usar schemas Pydantic com campos explícitos. Nunca passar `**request.json()` diretamente para o ORM.

```python
# SEGURO — schema controla quais campos são aceitos
user = User(**data.model_dump())

# INSEGURO — permite que o cliente envie qualquer campo (ex.: is_admin=True)
# user = User(**await request.json())
```

### 10.5 Path Traversal

**Prevenção:** Validar e restringir nomes de arquivo e caminhos.

```python
import os
from pathlib import Path

UPLOAD_DIR = Path("/app/uploads")


def safe_filepath(filename: str) -> Path:
    """Garante que o arquivo fica dentro do diretório permitido."""
    # Remove componentes de path traversal
    safe_name = Path(filename).name  # Extrai apenas o nome do arquivo

    if not safe_name or safe_name.startswith("."):
        raise ValueError("Nome de arquivo inválido")

    full_path = (UPLOAD_DIR / safe_name).resolve()

    # Verifica se continua dentro do diretório permitido
    if not str(full_path).startswith(str(UPLOAD_DIR.resolve())):
        raise ValueError("Path traversal detectado")

    return full_path
```

### 10.6 SSRF (Server-Side Request Forgery)

**Prevenção:** Se a API faz requisições a URLs fornecidas pelo usuário, validar contra endereços internos.

```python
# app/common/url_validation.py
import ipaddress
from urllib.parse import urlparse
import socket


BLOCKED_RANGES = [
    ipaddress.ip_network("10.0.0.0/8"),
    ipaddress.ip_network("172.16.0.0/12"),
    ipaddress.ip_network("192.168.0.0/16"),
    ipaddress.ip_network("127.0.0.0/8"),
    ipaddress.ip_network("169.254.0.0/16"),
    ipaddress.ip_network("0.0.0.0/8"),
]


def is_safe_url(url: str) -> bool:
    """Valida se a URL não aponta para rede interna."""
    parsed = urlparse(url)

    if parsed.scheme not in ("http", "https"):
        return False

    hostname = parsed.hostname
    if not hostname:
        return False

    try:
        ip = ipaddress.ip_address(socket.gethostbyname(hostname))
    except (socket.gaierror, ValueError):
        return False

    return not any(ip in network for network in BLOCKED_RANGES)
```

---

## 11. Gerenciamento de Dependências

### 11.1 Regras Obrigatórias

- Fixar **versões exatas** de dependências em produção (`package==1.2.3`).
- Rodar **auditoria de vulnerabilidades** regularmente.
- Manter dependências **atualizadas** — não ignorar patches de segurança.
- Usar **ambientes virtuais** isolados.
- Mínimo de dependências — cada pacote adicionado é uma superfície de ataque.

### 11.2 Comandos de Auditoria

```bash
# Verificar vulnerabilidades conhecidas
pip install pip-audit
pip-audit

# Verificar dependências desatualizadas
pip list --outdated

# Gerar requirements com versões fixas
pip freeze > requirements.lock

# Alternativa com safety
pip install safety
safety check
```

### 11.3 CI/CD — Checagem Automática

```yaml
# .github/workflows/security.yml
name: Security Audit
on:
  push:
    branches: [main]
  schedule:
    - cron: "0 6 * * 1"  # Toda segunda-feira

jobs:
  audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: "3.12"
      - run: |
          pip install pip-audit safety bandit
          pip install -r requirements.txt
      - run: pip-audit
      - run: safety check
      - run: bandit -r app/ -ll  # Análise estática de segurança
```

---

## 12. Segurança em Uploads de Arquivos

### 12.1 Regras Obrigatórias

- Validar **MIME type** e **extensão** do arquivo (verificar magic bytes, não confiar apenas na extensão).
- Limitar **tamanho máximo** do arquivo.
- **Renomear** o arquivo com UUID — nunca usar o nome original no filesystem.
- Armazenar uploads **fora** do diretório da aplicação.
- Escanear contra **malware** quando possível.
- Servir arquivos via **CDN/storage separado**, nunca diretamente pela API.
- Nunca executar ou interpretar o conteúdo de uploads.

### 12.2 Implementação Segura

```python
# app/core/file_upload.py
import uuid
from pathlib import Path

from fastapi import UploadFile, HTTPException, status

UPLOAD_DIR = Path("/data/uploads")
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

ALLOWED_MIME_TYPES = {
    "image/jpeg",
    "image/png",
    "image/webp",
    "application/pdf",
}

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".pdf"}

MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB

# Magic bytes para verificação real do tipo
MAGIC_BYTES = {
    b"\xff\xd8\xff": "image/jpeg",
    b"\x89PNG": "image/png",
    b"RIFF": "image/webp",
    b"%PDF": "application/pdf",
}


def _verify_magic_bytes(content: bytes) -> str | None:
    """Verifica o tipo real do arquivo pelos magic bytes."""
    for magic, mime in MAGIC_BYTES.items():
        if content[:len(magic)] == magic:
            return mime
    return None


async def save_upload(file: UploadFile) -> dict:
    """Processa e salva upload de forma segura."""

    # 1. Verificar extensão
    original_name = file.filename or "unknown"
    extension = Path(original_name).suffix.lower()
    if extension not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Extensão não permitida: {extension}",
        )

    # 2. Verificar MIME type declarado
    if file.content_type not in ALLOWED_MIME_TYPES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Tipo de arquivo não permitido: {file.content_type}",
        )

    # 3. Ler conteúdo com limite de tamanho
    content = await file.read()
    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"Arquivo excede o limite de {MAX_FILE_SIZE // (1024*1024)} MB",
        )

    # 4. Verificar magic bytes (tipo real do arquivo)
    real_mime = _verify_magic_bytes(content)
    if real_mime != file.content_type:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Conteúdo do arquivo não corresponde ao tipo declarado",
        )

    # 5. Salvar com nome seguro (UUID)
    safe_name = f"{uuid.uuid4().hex}{extension}"
    file_path = UPLOAD_DIR / safe_name
    file_path.write_bytes(content)

    return {
        "filename": safe_name,
        "original_name": original_name,
        "size": len(content),
        "content_type": real_mime,
        "path": str(file_path),
    }
```

---

## 13. Tratamento Seguro de Erros

### 13.1 Regras Obrigatórias

- **Nunca** expor stack traces, nomes de tabelas, queries SQL ou caminhos internos ao cliente.
- Respostas de erro devem ser **genéricas** para o cliente e **detalhadas** apenas nos logs internos.
- Usar mensagens de erro **uniformes** para falhas de autenticação (prevenir enumeração de usuários).
- Capturar exceções não tratadas globalmente.

### 13.2 Handler Global

```python
# app/core/exceptions.py
import traceback

from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError

from app.core.logging import logger


# Exceção base de domínio
class AppException(Exception):
    def __init__(self, message: str, status_code: int = 400):
        self.message = message
        self.status_code = status_code


# Handler para exceções de domínio
async def app_exception_handler(request: Request, exc: AppException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.message},
    )


# Handler para erros de validação (Pydantic)
async def validation_exception_handler(
    request: Request, exc: RequestValidationError
):
    # Logar detalhes para debug, retornar versão limpa ao cliente
    logger.warning("Validation error", extra={"errors": exc.errors()})

    # Sanitizar — não expor nomes internos de modelo
    safe_errors = []
    for error in exc.errors():
        safe_errors.append({
            "field": " → ".join(str(loc) for loc in error["loc"] if loc != "body"),
            "message": error["msg"],
        })

    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={"detail": safe_errors},
    )


# Handler global para exceções não tratadas
async def unhandled_exception_handler(request: Request, exc: Exception):
    # Log completo para investigação interna
    logger.error(
        "Unhandled exception",
        extra={
            "path": request.url.path,
            "method": request.method,
            "traceback": traceback.format_exc(),
        },
    )

    # Resposta genérica — sem detalhes internos
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"detail": "Erro interno do servidor"},
    )
```

```python
# app/main.py — Registro dos handlers
from fastapi.exceptions import RequestValidationError

app.add_exception_handler(AppException, app_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, unhandled_exception_handler)
```

---

## 14. HTTPS e Comunicação Segura

### 14.1 Regras Obrigatórias

- **Toda** comunicação em produção deve usar HTTPS (TLS 1.2+).
- Redirecionar HTTP → HTTPS automaticamente.
- Usar certificados válidos (Let's Encrypt ou CA comercial).
- Habilitar **HSTS** (via header, já configurado na seção 6).
- Comunicação entre serviços internos também deve ser criptografada quando possível.

### 14.2 Redirect HTTP → HTTPS

```python
# app/core/middleware.py

class HTTPSRedirectMiddleware(BaseHTTPMiddleware):
    """Redireciona HTTP para HTTPS em produção."""

    async def dispatch(self, request: Request, call_next):
        if (
            request.url.scheme == "http"
            and request.headers.get("x-forwarded-proto") != "https"
        ):
            url = request.url.replace(scheme="https")
            return JSONResponse(
                status_code=301,
                headers={"Location": str(url)},
                content={"detail": "Use HTTPS"},
            )
        return await call_next(request)
```

### 14.3 Uvicorn com SSL (Desenvolvimento)

```bash
uvicorn app.main:app \
    --host 0.0.0.0 \
    --port 443 \
    --ssl-keyfile ./key.pem \
    --ssl-certfile ./cert.pem
```

---

## 15. Configuração Segura do Ambiente

### 15.1 Regras Obrigatórias

- **Toda** secret via variável de ambiente — nunca no código-fonte.
- `.env` no `.gitignore` — nunca commitado.
- Manter `.env.example` atualizado com **chaves sem valores**.
- Usar **secrets diferentes** para cada ambiente (dev, staging, prod).
- `DEBUG=False` em produção — sempre.
- Desabilitar **documentação interativa** (Swagger/ReDoc) em produção, ou protegê-la com autenticação.

### 15.2 Configuração Completa

```python
# app/config.py
from pydantic import SecretStr, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
    )

    # App
    APP_NAME: str = "My API"
    APP_ENV: str = "development"  # development | staging | production
    DEBUG: bool = False

    # Secrets
    SECRET_KEY: SecretStr
    REFRESH_SECRET_KEY: SecretStr

    # Database
    DATABASE_URL: SecretStr
    DATABASE_ECHO: bool = False

    # Auth
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    # CORS
    ALLOWED_ORIGINS: list[str] = ["http://localhost:3000"]

    # Rate Limiting
    RATE_LIMIT_STORAGE: str = "memory://"  # "redis://..." em produção

    @field_validator("SECRET_KEY", "REFRESH_SECRET_KEY")
    @classmethod
    def validate_secret_length(cls, v: SecretStr) -> SecretStr:
        if len(v.get_secret_value()) < 32:
            raise ValueError("Secret key deve ter no mínimo 32 caracteres")
        return v

    @property
    def is_production(self) -> bool:
        return self.APP_ENV == "production"


settings = Settings()
```

### 15.3 Docs Protegidas em Produção

```python
# app/main.py
from app.config import settings


def create_app() -> FastAPI:
    # Desabilitar docs em produção (ou proteger com auth)
    docs_url = None if settings.is_production else "/docs"
    redoc_url = None if settings.is_production else "/redoc"

    app = FastAPI(
        title=settings.APP_NAME,
        debug=settings.DEBUG,
        docs_url=docs_url,
        redoc_url=redoc_url,
    )
    ...
    return app
```

### 15.4 `.env.example`

```env
# App
APP_NAME=My API
APP_ENV=development
DEBUG=false

# Secrets (gerar com: python -c "import secrets; print(secrets.token_urlsafe(64))")
SECRET_KEY=
REFRESH_SECRET_KEY=

# Database
DATABASE_URL=postgresql+asyncpg://user:password@localhost:5432/mydb
DATABASE_ECHO=false

# Auth
ACCESS_TOKEN_EXPIRE_MINUTES=15
REFRESH_TOKEN_EXPIRE_DAYS=7

# CORS
ALLOWED_ORIGINS=["http://localhost:3000"]

# Rate Limiting
RATE_LIMIT_STORAGE=memory://
```

---

## 16. Checklist de Segurança para o Agente de IA

Antes de gerar ou revisar código, verificar **todos** os itens:

### Autenticação e Autorização
```
[ ] Senhas hasheadas com bcrypt ou argon2?
[ ] JWT com expiração curta (≤30 min) e claims validadas (exp, iss, aud)?
[ ] Refresh token com rotação implementada?
[ ] Toda rota protegida verifica autenticação via Depends()?
[ ] IDOR protegido — usuário só acessa recursos próprios?
[ ] Roles/permissões verificadas antes de ações privilegiadas?
[ ] Login protegido contra timing attack (DUMMY_HASH)?
[ ] Account lockout após tentativas falhas?
```

### Validação de Entrada
```
[ ] Toda entrada validada com schema Pydantic?
[ ] Campos com min_length, max_length, ge, le, pattern?
[ ] Strings sanitizadas contra XSS e caracteres de controle?
[ ] Paginação com limites máximos (per_page ≤ 100)?
[ ] Body size limit configurado?
[ ] Parâmetros de busca sanitizados contra SQL wildcards?
```

### Dados e Comunicação
```
[ ] Nenhum campo sensível nos schemas de resposta?
[ ] Nenhum dado sensível nos logs?
[ ] HTTPS obrigatório em produção?
[ ] CORS com origens explícitas (sem wildcard)?
[ ] Headers de segurança presentes em todas as respostas?
[ ] Cookies com HttpOnly, Secure, SameSite?
```

### Banco de Dados
```
[ ] Queries via ORM ou parametrizadas (sem concatenação)?
[ ] Conexão com SSL em produção?
[ ] Credenciais via variáveis de ambiente?
[ ] Pool de conexões configurado?
```

### Erros e Logs
```
[ ] Stack traces nunca expostos ao cliente?
[ ] Erros de autenticação com mensagem genérica?
[ ] Handler global para exceções não tratadas?
[ ] Eventos de segurança auditados (login, logout, alterações)?
[ ] Request ID em todos os logs?
```

### Uploads
```
[ ] Magic bytes verificados (não apenas extensão)?
[ ] Tamanho máximo limitado?
[ ] Arquivo renomeado com UUID?
[ ] Armazenado fora do diretório da aplicação?
```

### Infraestrutura
```
[ ] Secrets diferentes por ambiente?
[ ] DEBUG=False em produção?
[ ] Swagger/ReDoc desabilitados ou protegidos em produção?
[ ] Dependências auditadas contra vulnerabilidades?
[ ] Rate limiting em endpoints públicos e de autenticação?
```

---

## Referências

| Recurso | URL |
|---------|-----|
| OWASP API Security Top 10 | https://owasp.org/API-Security/ |
| OWASP Cheat Sheet Series | https://cheatsheetseries.owasp.org/ |
| FastAPI Security Docs | https://fastapi.tiangolo.com/tutorial/security/ |
| CWE Top 25 | https://cwe.mitre.org/top25/ |
| Python Bandit (SAST) | https://bandit.readthedocs.io/ |
