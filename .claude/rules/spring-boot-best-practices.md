# Melhores Práticas de Desenvolvimento — Java Spring Boot

> **Propósito:** Referência completa para agentes de IA e desenvolvedores gerarem código Java/Spring Boot organizado, seguro, performático e manutenível. Cada seção contém regras obrigatórias, justificativa e exemplos de implementação.

---

## Índice

1. [Estrutura de Projeto](#1-estrutura-de-projeto)
2. [Organização de Código e Camadas](#2-organização-de-código-e-camadas)
3. [Padrões de Projeto no Spring Boot](#3-padrões-de-projeto-no-spring-boot)
4. [Configuração e Gerenciamento de Dependências](#4-configuração-e-gerenciamento-de-dependências)
5. [APIs REST — Boas Práticas](#5-apis-rest--boas-práticas)
6. [DTOs e Mapeamento](#6-dtos-e-mapeamento)
7. [Validação de Dados](#7-validação-de-dados)
8. [Tratamento de Exceções](#8-tratamento-de-exceções)
9. [Segurança — Spring Security](#9-segurança--spring-security)
10. [Integração com Banco de Dados — JPA/Hibernate](#10-integração-com-banco-de-dados--jpahibernate)
11. [Migrations com Flyway](#11-migrations-com-flyway)
12. [Testes — Unitários e Integração](#12-testes--unitários-e-integração)
13. [Logging e Monitoramento](#13-logging-e-monitoramento)
14. [Performance e Escalabilidade](#14-performance-e-escalabilidade)
15. [Configuração por Ambiente](#15-configuração-por-ambiente)
16. [Docker e Deploy](#16-docker-e-deploy)
17. [Checklist para o Agente de IA](#17-checklist-para-o-agente-de-ia)

---

## 1. Estrutura de Projeto

### 1.1 Organização por Feature (Recomendada)

```
src/
├── main/
│   ├── java/com/example/myapp/
│   │   ├── MyAppApplication.java              # Classe principal (@SpringBootApplication)
│   │   │
│   │   ├── common/                            # Código transversal compartilhado
│   │   │   ├── config/                        # Configurações globais
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── exception/                     # Exceções base e handler global
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── security/                      # JWT, filtros, auth
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── audit/                         # Auditoria
│   │   │   │   └── AuditableEntity.java
│   │   │   ├── pagination/                    # Utilitários de paginação
│   │   │   │   └── PageResponse.java
│   │   │   └── util/                          # Funções utilitárias
│   │   │       ├── DateUtils.java
│   │   │       └── SlugUtils.java
│   │   │
│   │   ├── user/                              # Feature: Usuários
│   │   │   ├── controller/
│   │   │   │   └── UserController.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java           # Interface
│   │   │   │   └── UserServiceImpl.java       # Implementação
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java
│   │   │   ├── entity/
│   │   │   │   └── User.java
│   │   │   ├── dto/
│   │   │   │   ├── UserCreateRequest.java
│   │   │   │   ├── UserUpdateRequest.java
│   │   │   │   └── UserResponse.java
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java
│   │   │   └── exception/
│   │   │       ├── UserNotFoundException.java
│   │   │       └── UserAlreadyExistsException.java
│   │   │
│   │   ├── product/                           # Feature: Produtos
│   │   │   └── ...                            # Mesma estrutura
│   │   │
│   │   └── order/                             # Feature: Pedidos
│   │       └── ...
│   │
│   └── resources/
│       ├── application.yml                    # Configuração principal
│       ├── application-dev.yml                # Configuração de desenvolvimento
│       ├── application-prod.yml               # Configuração de produção
│       ├── application-test.yml               # Configuração de teste
│       └── db/migration/                      # Flyway migrations
│           ├── V1__create_users_table.sql
│           ├── V2__create_products_table.sql
│           └── V3__create_orders_table.sql
│
└── test/
    └── java/com/example/myapp/
        ├── user/
        │   ├── controller/
        │   │   └── UserControllerTest.java     # Teste de integração (MockMvc)
        │   ├── service/
        │   │   └── UserServiceTest.java        # Teste unitário
        │   └── repository/
        │       └── UserRepositoryTest.java     # Teste de integração (DataJpaTest)
        └── common/
            └── ...
```

### 1.2 Regras de Organização

- Organizar por **feature** (domínio), não por camada técnica.
- Cada feature contém suas próprias camadas: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `mapper/`, `exception/`.
- Código compartilhado entre features fica em `common/`.
- **Nunca** uma feature importa de outra diretamente pelo repository — comunicação entre features via Service.
- Um arquivo por classe — nome do arquivo igual ao nome da classe.
- Testes espelham a estrutura do código principal.

### 1.3 Convenções de Nomes

| Elemento | Convenção | Exemplo |
|---|---|---|
| Packages | lowercase, singular | `com.example.myapp.user` |
| Classes | PascalCase | `UserService`, `OrderController` |
| Interfaces | PascalCase (sem prefixo I) | `UserService` (impl: `UserServiceImpl`) |
| Métodos | camelCase | `findByEmail`, `createUser` |
| Constantes | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS` |
| Variáveis | camelCase | `userId`, `orderTotal` |
| DTOs de entrada | `{Entity}CreateRequest`, `{Entity}UpdateRequest` | `UserCreateRequest` |
| DTOs de saída | `{Entity}Response` | `UserResponse` |
| Tabelas no banco | snake_case, plural | `users`, `order_items` |
| Colunas no banco | snake_case | `created_at`, `full_name` |
| Endpoints | kebab-case, plural | `/api/v1/order-items` |

---

## 2. Organização de Código e Camadas

### 2.1 Fluxo de Responsabilidades

```
Request HTTP → Controller → Service → Repository → Database
                   ↕            ↕
                  DTO         Entity
```

| Camada | Responsabilidade | Anotação |
|---|---|---|
| **Controller** | Receber HTTP, validar entrada, retornar resposta | `@RestController` |
| **Service** | Regras de negócio, orquestração, transações | `@Service` |
| **Repository** | Acesso a dados, queries | `@Repository` (ou extends `JpaRepository`) |
| **Entity** | Mapeamento ORM (tabela ↔ objeto) | `@Entity` |
| **DTO** | Transporte de dados (request/response) | Records ou classes |
| **Mapper** | Conversão entre Entity e DTO | `@Component` ou MapStruct |

### 2.2 Regras por Camada

**Controller:**
- Recebe DTOs de request, **nunca** Entities.
- Retorna DTOs de response, **nunca** Entities.
- Não contém lógica de negócio — apenas delega ao Service.
- Define `@ResponseStatus`, `@Valid`, path variables e query params.
- Documentação via `@Operation` (OpenAPI/Swagger).

**Service:**
- Contém **toda** a lógica de negócio.
- Definido como interface + implementação para testabilidade.
- Gerencia **transações** (`@Transactional`).
- Lança **exceções de domínio** (nunca `ResponseStatusException`).
- Pode chamar outros Services — nunca acessar repositórios de outras features.

**Repository:**
- Apenas acesso a dados — sem lógica de negócio.
- Extends `JpaRepository<Entity, IdType>` para CRUD automático.
- Query methods nomeados ou `@Query` para consultas customizadas.
- Retorna Entities, nunca DTOs.

---

## 3. Padrões de Projeto no Spring Boot

### 3.1 Dependency Injection (IoC)

```java
// CORRETO — Injeção via construtor (preferido)
@Service
@RequiredArgsConstructor  // Lombok gera o construtor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // O Spring injeta automaticamente pelo construtor
}

// EVITAR — Injeção por campo (@Autowired)
@Service
public class UserServiceImpl implements UserService {
    @Autowired  // ❌ Dificulta testes, oculta dependências
    private UserRepository userRepository;
}
```

### 3.2 Builder Pattern (com Lombok)

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // ...
}

// Uso
User user = User.builder()
    .email(request.email())
    .fullName(request.fullName())
    .hashedPassword(passwordEncoder.encode(request.password()))
    .isActive(true)
    .build();
```

### 3.3 Strategy Pattern (com Spring)

```java
// Interface comum
public interface NotificationStrategy {
    void send(String recipient, String message);
    NotificationType getType();
}

// Implementações
@Component
public class EmailNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(String recipient, String message) { /* email */ }

    @Override
    public NotificationType getType() { return NotificationType.EMAIL; }
}

@Component
public class SmsNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(String recipient, String message) { /* SMS */ }

    @Override
    public NotificationType getType() { return NotificationType.SMS; }
}

// Resolver — Spring injeta todas as implementações
@Component
@RequiredArgsConstructor
public class NotificationStrategyResolver {

    private final List<NotificationStrategy> strategies;

    public NotificationStrategy resolve(NotificationType type) {
        return strategies.stream()
            .filter(s -> s.getType() == type)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Estratégia não encontrada: " + type));
    }
}
```

### 3.4 Template Method (com abstract class)

```java
public abstract class BaseService<E, ID> {

    protected abstract JpaRepository<E, ID> getRepository();

    public E findByIdOrThrow(ID id) {
        return getRepository().findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                getEntityName(), id));
    }

    protected abstract String getEntityName();
}

@Service
public class UserServiceImpl extends BaseService<User, UUID>
    implements UserService {

    private final UserRepository userRepository;

    @Override
    protected JpaRepository<User, UUID> getRepository() {
        return userRepository;
    }

    @Override
    protected String getEntityName() {
        return "User";
    }
}
```

### 3.5 Specification Pattern (Queries Dinâmicas)

```java
// Útil para filtros dinâmicos de listagem
public class UserSpecifications {

    public static Specification<User> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<User> emailContains(String email) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> nameContains(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("fullName")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }
}

// Uso no Service
public Page<User> findFiltered(UserFilterRequest filter, Pageable pageable) {
    Specification<User> spec = Specification.where(isActive());

    if (filter.email() != null) {
        spec = spec.and(emailContains(filter.email()));
    }
    if (filter.name() != null) {
        spec = spec.and(nameContains(filter.name()));
    }
    if (filter.role() != null) {
        spec = spec.and(hasRole(filter.role()));
    }

    return userRepository.findAll(spec, pageable);
}
```

---

## 4. Configuração e Gerenciamento de Dependências

### 4.1 Maven — pom.xml Recomendado

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>myapp</artifactId>
    <version>1.0.0</version>
    <name>My Application</name>
    <description>Spring Boot Application</description>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.2</mapstruct.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Validação -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- JPA / Hibernate -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Actuator (monitoramento) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway (migrations) -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- MapStruct (mapeamento DTO ↔ Entity) -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- OpenAPI / Swagger -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.6.0</version>
        </dependency>

        <!-- Testes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <!-- Lombok + MapStruct binding -->
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 4.2 Gradle — build.gradle.kts Equivalente

```kotlin
// build.gradle.kts
plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("org.mapstruct:mapstruct:1.6.2")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### 4.3 Regras de Dependências

- Usar **Spring Boot BOM** (parent) para gerenciar versões — nunca fixar versão de starters manualmente.
- Separar scopes: `runtime` para drivers, `test` para dependências de teste, `compileOnly` para Lombok.
- **Nunca** incluir dependências não utilizadas — cada dependência é superfície de ataque.
- Auditar vulnerabilidades com `mvn dependency-check:check` ou `gradle dependencyCheckAnalyze`.

---

## 5. APIs REST — Boas Práticas

### 5.1 Regras Obrigatórias

- URLs em **kebab-case**, substantivos no **plural**: `/api/v1/order-items`.
- Usar **verbos HTTP corretos**: GET (leitura), POST (criação), PUT (substituição completa), PATCH (atualização parcial), DELETE (remoção).
- Retornar **status codes corretos** (tabela abaixo).
- Versionar a API no path: `/api/v1/`, `/api/v2/`.
- Paginação em toda listagem — **nunca** retornar coleção completa sem limite.
- Retornar DTOs, **nunca** Entities diretamente.
- Usar `@Valid` em **todos** os request bodies.

### 5.2 Status Codes

| Operação | Sucesso | Erro Comum |
|---|---|---|
| GET (item) | `200 OK` | `404 Not Found` |
| GET (lista) | `200 OK` | — |
| POST (criar) | `201 Created` | `400 Bad Request`, `409 Conflict` |
| PUT/PATCH | `200 OK` | `400`, `404`, `409` |
| DELETE | `204 No Content` | `404 Not Found` |
| Autenticação falha | — | `401 Unauthorized` |
| Sem permissão | — | `403 Forbidden` |
| Validação falha | — | `422 Unprocessable Entity` |
| Rate limit | — | `429 Too Many Requests` |

### 5.3 Controller Completo

```java
package com.example.myapp.user.controller;

import com.example.myapp.common.pagination.PageResponse;
import com.example.myapp.user.dto.UserCreateRequest;
import com.example.myapp.user.dto.UserResponse;
import com.example.myapp.user.dto.UserUpdateRequest;
import com.example.myapp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Listar usuários paginados")
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        var page = userService.findAll(search, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        var user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request) {

        var user = userService.create(request);
        var uri = URI.create("/api/v1/users/" + user.id());
        return ResponseEntity.created(uri).body(user);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {

        var user = userService.update(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir usuário")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
```

### 5.4 Resposta Paginada Padrão

```java
package com.example.myapp.common.pagination;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
    List<T> items,
    int page,
    int perPage,
    long total,
    int totalPages,
    boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber() + 1,  // Spring é 0-based, API é 1-based
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
    }
}
```

---

## 6. DTOs e Mapeamento

### 6.1 Regras Obrigatórias

- Usar **Java Records** para DTOs imutáveis.
- DTOs separados para **request** (entrada) e **response** (saída).
- **Nunca** expor Entities na API — sempre mapear para DTO.
- **Nunca** incluir campos sensíveis no DTO de resposta (senha, tokens).
- Usar **MapStruct** para mapeamento Entity ↔ DTO (evitar código manual repetitivo).
- DTOs de update devem ter campos **nullable** (parcial update).

### 6.2 DTOs como Records

```java
// Request — Criação
package com.example.myapp.user.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String fullName,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 128, message = "Senha deve ter entre 8 e 128 caracteres")
    String password,

    @NotBlank(message = "Perfil é obrigatório")
    @Pattern(regexp = "^(user|manager|admin)$", message = "Perfil inválido")
    String role
) {}


// Request — Atualização (campos opcionais)
public record UserUpdateRequest(
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String fullName,

    @Email(message = "Email inválido")
    String email,

    Boolean isActive
) {}


// Response — Nunca expor senha ou dados sensíveis
public record UserResponse(
    UUID id,
    String fullName,
    String email,
    String role,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### 6.3 Mapper com MapStruct

```java
package com.example.myapp.user.mapper;

import com.example.myapp.user.dto.UserCreateRequest;
import com.example.myapp.user.dto.UserResponse;
import com.example.myapp.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hashedPassword", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    User toEntity(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hashedPassword", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
}
```

---

## 7. Validação de Dados

### 7.1 Regras Obrigatórias

- **Sempre** usar `@Valid` nos request bodies do Controller.
- Validar **no DTO** com Bean Validation (`jakarta.validation`).
- Criar **validações customizadas** para regras complexas (CPF, CNPJ, senha forte).
- Validar **regras de negócio** no Service (unicidade, existência de relacionamentos).
- Retornar erros de validação em formato **padronizado** (tratado no GlobalExceptionHandler).

### 7.2 Anotações Padrão

```java
// Anotações mais usadas
@NotNull        // Não nulo
@NotBlank       // Não nulo, não vazio, não só espaços (Strings)
@NotEmpty       // Não nulo, não vazio (Strings, Collections)
@Size(min, max) // Tamanho de String, Collection, Array
@Min / @Max     // Valor mínimo/máximo numérico
@Positive       // Número positivo
@Email          // Formato de email
@Pattern        // Regex customizado
@Past / @Future // Datas no passado/futuro
@Valid          // Ativar validação em objeto aninhado
```

### 7.3 Validador Customizado

```java
// Anotação
package com.example.myapp.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Senha não atende os requisitos de segurança";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


// Implementação do validador
package com.example.myapp.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator
    implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;  // @NotBlank cuida do nulo

        boolean hasUpper = value.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = value.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        boolean hasMinLength = value.length() >= 8;

        return hasUpper && hasLower && hasDigit && hasSpecial && hasMinLength;
    }
}


// Uso no DTO
public record UserCreateRequest(
    @NotBlank
    @StrongPassword
    String password
) {}
```

---

## 8. Tratamento de Exceções

### 8.1 Regras Obrigatórias

- Tratar exceções em **um único lugar**: `@RestControllerAdvice` (GlobalExceptionHandler).
- Exceções de domínio herdam de uma `BusinessException` base.
- **Nunca** expor stack traces, nomes de classes internas ou queries SQL ao cliente.
- Respostas de erro em formato **padronizado e consistente**.
- Services lançam exceções de domínio — Controllers **não** fazem try/catch.
- Logar internamente detalhes completos; retornar ao cliente apenas mensagens genéricas seguras.

### 8.2 Exceções Base

```java
// Exceção base de domínio
package com.example.myapp.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;

    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}


// Recurso não encontrado
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s não encontrado: %s".formatted(resource, id),
              HttpStatus.NOT_FOUND);
    }
}


// Conflito (duplicata)
public class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}


// Acesso negado (regra de negócio)
public class AccessDeniedException extends BusinessException {

    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
```

```java
// Exceções específicas da feature
package com.example.myapp.user.exception;

import com.example.myapp.common.exception.ResourceNotFoundException;
import com.example.myapp.common.exception.ResourceAlreadyExistsException;
import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(UUID id) {
        super("User", id);
    }
}

public class UserAlreadyExistsException extends ResourceAlreadyExistsException {
    public UserAlreadyExistsException(String email) {
        super("Já existe um usuário com o email: " + email);
    }
}
```

### 8.3 Resposta de Erro Padronizada

```java
package com.example.myapp.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path,
                                 LocalDateTime.now(), null);
    }

    public static ErrorResponse withFieldErrors(int status, String error,
            String message, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(status, error, message, path,
                                 LocalDateTime.now(), fieldErrors);
    }
}
```

### 8.4 GlobalExceptionHandler

```java
package com.example.myapp.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Exceções de negócio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception: {}", ex.getMessage());

        var error = ErrorResponse.of(
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Erros de validação (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();

        var error = ErrorResponse.withFieldErrors(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Validation Error",
            "Erro de validação nos campos informados",
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.unprocessableEntity().body(error);
    }

    // Tipo de parâmetro inválido (ex.: UUID mal formatado)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        var error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Parâmetro inválido: " + ex.getName(),
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // Acesso negado (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        var error = ErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "Acesso negado",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Exceções não tratadas (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {} {}", request.getMethod(),
                  request.getRequestURI(), ex);

        var error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Erro interno do servidor",  // Genérico — sem detalhes internos
            request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(error);
    }
}
```

---

## 9. Segurança — Spring Security

### 9.1 Regras Obrigatórias

- Usar **JWT** para autenticação stateless em APIs REST.
- Senhas **sempre** hasheadas com BCrypt (mínimo strength 12).
- Access token com expiração curta (15–30 min), refresh token com expiração longa (7–30 dias).
- Validar **todas** as claims do JWT: `exp`, `sub`, `iss`.
- Proteger endpoints com **roles/authorities** via `@PreAuthorize` ou `SecurityFilterChain`.
- **CORS** configurado com origens explícitas — nunca `*` em produção.
- **CSRF** desabilitado apenas para APIs stateless (JWT no header).

### 9.2 SecurityConfig

```java
package com.example.myapp.common.config;

import com.example.myapp.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Habilita @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)  // Desabilitado para API stateless
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rotas públicas
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Rotas protegidas por role
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**")
                    .hasRole("ADMIN")

                // Todas as demais requerem autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000"  // Frontend dev
            // Adicionar origens de produção via config
        ));
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of(
            "Authorization", "Content-Type", "X-Request-ID"));
        configuration.setExposedHeaders(List.of(
            "X-Request-ID", "X-RateLimit-Remaining"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### 9.3 JWT Token Provider

```java
package com.example.myapp.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${security.jwt.access-secret}") String accessSecret,
            @Value("${security.jwt.refresh-secret}") String refreshSecret,
            @Value("${security.jwt.access-expiration-minutes:15}") long accessMinutes,
            @Value("${security.jwt.refresh-expiration-days:7}") long refreshDays,
            @Value("${spring.application.name}") String appName) {

        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessMinutes;
        this.refreshTokenDays = refreshDays;
        this.issuer = appName;
    }

    public String generateAccessToken(UUID userId, String role) {
        var now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .claim("role", role)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTokenMinutes, ChronoUnit.MINUTES)))
            .signWith(accessKey)
            .compact();
    }

    public String generateRefreshToken(UUID userId) {
        var now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshTokenDays, ChronoUnit.DAYS)))
            .signWith(refreshKey)
            .compact();
    }

    public Claims parseAccessToken(String token) {
        return parse(token, accessKey);
    }

    public Claims parseRefreshToken(String token) {
        return parse(token, refreshKey);
    }

    private Claims parse(String token, SecretKey key) {
        return Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

### 9.4 JWT Authentication Filter

```java
package com.example.myapp.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null) {
                var claims = tokenProvider.parseAccessToken(token);
                var userId = claims.getSubject();
                var role = claims.get("role", String.class);

                var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                var authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.debug("JWT authentication failed: {}", ex.getMessage());
            // Não lançar exceção — deixar o Spring Security retornar 401
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

---

## 10. Integração com Banco de Dados — JPA/Hibernate

### 10.1 Regras Obrigatórias

- Usar **UUID** como tipo de ID (distribuído, seguro).
- **Sempre** usar `TIMESTAMPTZ` (timezone-aware) para timestamps.
- Entities devem ter `createdAt` e `updatedAt` com auditoria automática.
- **Nunca** usar `FetchType.EAGER` em relacionamentos — sempre `LAZY`.
- Usar **`@EntityGraph`** ou **`JOIN FETCH`** quando precisar de dados relacionados.
- **Nunca** expor Entities na API — mapear para DTOs.
- Usar **Specification** para queries dinâmicas.
- Nomear constraints explicitamente para mensagens de erro claras.

### 10.2 Entity Base com Auditoria

```java
package com.example.myapp.common.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

```java
// Habilitar auditoria
package com.example.myapp.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

### 10.3 Entity de Domínio

```java
package com.example.myapp.user.entity;

import com.example.myapp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uq_users_email", columnNames = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // Relacionamento LAZY (padrão obrigatório)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private java.util.List<com.example.myapp.order.entity.Order> orders;
}
```

### 10.4 Repository com Queries Customizadas

```java
package com.example.myapp.user.repository;

import com.example.myapp.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends
        JpaRepository<User, UUID>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE u.isActive = true
          AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<User> searchActive(String search, Pageable pageable);

    // Evitar N+1 com EntityGraph
    @EntityGraph(attributePaths = {"orders"})
    Optional<User> findWithOrdersById(UUID id);
}
```

### 10.5 application.yml — JPA/Hibernate

```yaml
spring:
  jpa:
    open-in-view: false  # OBRIGATÓRIO: desabilitar OSIV (anti-pattern)
    hibernate:
      ddl-auto: validate  # Nunca 'create' ou 'update' em produção
    properties:
      hibernate:
        format_sql: false
        default_batch_fetch_size: 20  # Mitigar N+1
        jdbc:
          batch_size: 50              # Batch inserts
        order_inserts: true
        order_updates: true
    show-sql: false  # true apenas em dev
```

### 10.6 Prevenção de N+1

```java
// PROBLEMA: N+1 queries
List<User> users = userRepository.findAll();
for (User user : users) {
    user.getOrders().size();  // ❌ 1 query por usuário
}


// SOLUÇÃO 1: @EntityGraph (no repository)
@EntityGraph(attributePaths = {"orders"})
List<User> findAllWithOrders();


// SOLUÇÃO 2: JOIN FETCH (JPQL)
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.isActive = true")
List<User> findActiveWithOrders();


// SOLUÇÃO 3: Batch fetching (global via config)
// hibernate.default_batch_fetch_size: 20
// Agrupa lazy loads em batches de 20 IDs (IN clause)


// SOLUÇÃO 4: Buscar IDs separadamente (melhor para coleções grandes)
List<UUID> userIds = userRepository.findActiveUserIds();
List<Order> orders = orderRepository.findByUserIdIn(userIds);
```

---

## 11. Migrations com Flyway

### 11.1 Regras Obrigatórias

- **Sempre** usar migrations para alterações de schema — nunca `ddl-auto: update`.
- Nomear com **versão sequencial**: `V1__create_users_table.sql`.
- **Nunca** alterar uma migration já aplicada — criar uma nova.
- Uma **alteração lógica** por migration.
- Testar `migrate` localmente antes de aplicar em staging/prod.
- Usar migration separada para dados iniciais (seed).

### 11.2 Configuração

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

### 11.3 Exemplos de Migrations

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name   VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'user',
    is_active   BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('user', 'manager', 'admin'))
);

CREATE INDEX ix_users_email ON users (email);
CREATE INDEX ix_users_active ON users (email) WHERE is_active = true;
```

```sql
-- V2__create_products_table.sql
CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    sku         VARCHAR(50) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT true,
    metadata    JSONB DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,

    CONSTRAINT uq_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price_positive CHECK (price > 0)
);

CREATE INDEX ix_products_sku ON products (sku);
CREATE INDEX ix_products_metadata ON products USING gin (metadata jsonb_path_ops);
```

```sql
-- V3__add_phone_to_users.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

---

## 12. Testes — Unitários e Integração

### 12.1 Regras Obrigatórias

- Testar Services com **testes unitários** (Mockito para dependências).
- Testar Controllers com **testes de integração** (`@WebMvcTest` + MockMvc).
- Testar Repositories com **`@DataJpaTest`** (banco em memória ou Testcontainers).
- Nomear testes: `deve{Ação}_quando{Condição}` ou `shouldDoX_whenConditionY`.
- Coverage mínimo: **80%** em Services e Controllers.
- Usar **Testcontainers** para testes de integração com banco real (PostgreSQL).

### 12.2 Teste Unitário de Service

```java
package com.example.myapp.user.service;

import com.example.myapp.user.dto.UserCreateRequest;
import com.example.myapp.user.entity.User;
import com.example.myapp.user.exception.UserAlreadyExistsException;
import com.example.myapp.user.exception.UserNotFoundException;
import com.example.myapp.user.mapper.UserMapper;
import com.example.myapp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve criar usuário quando email não existe")
        void shouldCreateUser_whenEmailIsNew() {
            // Given
            var request = new UserCreateRequest(
                "John Doe", "john@example.com", "Secure@123", "user");
            var user = User.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .build();

            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("hashed");
            given(userMapper.toEntity(request)).willReturn(user);
            given(userRepository.save(any(User.class))).willReturn(user);
            given(userMapper.toResponse(user)).willReturn(/* response mock */);

            // When
            var result = userService.create(request);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode(request.password());
        }

        @Test
        @DisplayName("deve lançar exceção quando email já existe")
        void shouldThrowException_whenEmailAlreadyExists() {
            // Given
            var request = new UserCreateRequest(
                "John Doe", "john@example.com", "Secure@123", "user");
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("deve retornar usuário quando existe")
        void shouldReturnUser_whenExists() {
            var id = UUID.randomUUID();
            var user = User.builder().id(id).fullName("John").build();
            given(userRepository.findById(id)).willReturn(Optional.of(user));

            var result = userService.findById(id);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("deve lançar exceção quando não existe")
        void shouldThrowException_whenNotFound() {
            var id = UUID.randomUUID();
            given(userRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UserNotFoundException.class);
        }
    }
}
```

### 12.3 Teste de Integração do Controller

```java
package com.example.myapp.user.controller;

import com.example.myapp.user.dto.UserCreateRequest;
import com.example.myapp.user.dto.UserResponse;
import com.example.myapp.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/users - deve criar usuário com sucesso")
    void shouldCreateUser() throws Exception {
        // Given
        var request = new UserCreateRequest(
            "John Doe", "john@example.com", "Secure@123", "user");
        var response = new UserResponse(
            UUID.randomUUID(), "John Doe", "john@example.com",
            "user", true, LocalDateTime.now(), null);

        given(userService.create(any())).willReturn(response);

        // When / Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fullName").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.hashedPassword").doesNotExist());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/users - deve retornar 422 quando validação falha")
    void shouldReturn422_whenValidationFails() throws Exception {
        var request = new UserCreateRequest("", "invalid-email", "123", "");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }
}
```

### 12.4 Teste de Repository com Testcontainers

```java
package com.example.myapp.user.repository;

import com.example.myapp.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByEmail() {
        // Given
        var user = User.builder()
            .fullName("John Doe")
            .email("john@example.com")
            .hashedPassword("hashed")
            .role("user")
            .isActive(true)
            .build();
        userRepository.save(user);

        // When
        var found = userRepository.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldReturnEmpty_whenEmailNotFound() {
        var found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }
}
```

---

## 13. Logging e Monitoramento

### 13.1 Regras Obrigatórias

- Usar **SLF4J** com **Logback** (padrão do Spring Boot) — nunca `System.out.println`.
- Usar `@Slf4j` (Lombok) para obter o logger.
- Níveis de log: `DEBUG` para desenvolvimento, `INFO` para operações normais, `WARN` para suspeitas, `ERROR` para falhas.
- **Nunca** logar dados sensíveis (senhas, tokens, CPF completo, dados de cartão).
- Usar **MDC (Mapped Diagnostic Context)** para request ID em todos os logs.
- Habilitar **Spring Actuator** para health checks e métricas.

### 13.2 Configuração de Logging

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.example.myapp: DEBUG  # Apenas em dev
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{requestId}] %-5level %logger{36} - %msg%n"
```

### 13.3 MDC Filter para Request ID

```java
package com.example.myapp.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)  // Executar antes de outros filtros
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;

        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_KEY, requestId);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
```

### 13.4 Spring Actuator

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized  # Detalhes apenas para autenticados
  metrics:
    tags:
      application: ${spring.application.name}
```

### 13.5 Uso Correto do Logger

```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public UserResponse create(UserCreateRequest request) {
        log.info("Criando usuário com email: {}", maskEmail(request.email()));

        // ... lógica

        log.info("Usuário criado com sucesso: id={}", user.getId());
        return userMapper.toResponse(user);
    }

    // CORRETO — Parâmetro lazy (não concatena se nível desabilitado)
    log.debug("Processando filtro: search={}, page={}", search, page);

    // ERRADO — Concatenação sempre executada
    log.debug("Processando filtro: search=" + search + ", page=" + page);

    // CORRETO — Exception como último parâmetro
    log.error("Erro ao processar pedido: orderId={}", orderId, exception);

    // NUNCA — Dados sensíveis
    // log.info("Login: email={}, password={}", email, password);  ❌
}
```

---

## 14. Performance e Escalabilidade

### 14.1 Regras Obrigatórias

- **Desabilitar** `spring.jpa.open-in-view` (OSIV) — anti-pattern que abre sessão no controller.
- Usar **paginação** em toda listagem (`Pageable`).
- Usar **cache** (Spring Cache + Redis) para dados lidos com frequência e alterados raramente.
- Usar **connection pool** adequado (HikariCP é o padrão do Spring Boot).
- **Async** para operações que não precisam de resposta imediata (emails, notificações).
- Monitorar queries lentas e N+1 com `hibernate.generate_statistics`.
- **Compressão** de resposta habilitada (gzip).

### 14.2 Cache com Spring Cache

```java
// Habilitar cache na aplicação
@Configuration
@EnableCaching
public class CacheConfig {
}

// Uso no Service
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Cacheable(value = "products", key = "#id")
    public ProductResponse findById(UUID id) {
        log.debug("Buscando produto no banco: {}", id);
        return productRepository.findById(id)
            .map(productMapper::toResponse)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @CacheEvict(value = "products", key = "#id")
    public ProductResponse update(UUID id, ProductUpdateRequest request) {
        // Invalida cache ao atualizar
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteAll() {
        // Invalida todo o cache da coleção
    }
}
```

### 14.3 Operações Assíncronas

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationServiceImpl implements NotificationService {

    @Async
    public void sendWelcomeEmail(String email, String name) {
        // Executa em thread separada — não bloqueia o request
        log.info("Enviando email de boas-vindas para: {}", maskEmail(email));
        // ... envio de email
    }
}
```

### 14.4 HikariCP

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000       # 5 min
      connection-timeout: 30000  # 30 seg
      max-lifetime: 1800000      # 30 min
      leak-detection-threshold: 60000  # Alertar se conexão aberta > 60s
```

### 14.5 Compressão de Resposta

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/plain
    min-response-size: 1024  # Comprimir respostas > 1KB
```

---

## 15. Configuração por Ambiente

### 15.1 application.yml Principal

```yaml
spring:
  application:
    name: myapp

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /

security:
  jwt:
    access-secret: ${JWT_ACCESS_SECRET}
    refresh-secret: ${JWT_REFRESH_SECRET}
    access-expiration-minutes: ${JWT_ACCESS_EXPIRATION:15}
    refresh-expiration-days: ${JWT_REFRESH_EXPIRATION:7}
```

### 15.2 application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp_dev
    username: postgres
    password: postgres
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  flyway:
    clean-on-validation-error: true  # Apenas em dev

logging:
  level:
    com.example.myapp: DEBUG
    org.hibernate.SQL: DEBUG

springdoc:
  swagger-ui:
    enabled: true
```

### 15.3 application-prod.yml

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false

logging:
  level:
    root: WARN
    com.example.myapp: INFO

springdoc:
  swagger-ui:
    enabled: false  # Desabilitar em produção

server:
  compression:
    enabled: true
```

---

## 16. Docker e Deploy

### 16.1 Dockerfile Multi-Stage

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Cache de dependências
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Segurança: rodar como non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 16.2 docker-compose.yml (Desenvolvimento)

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DATABASE_URL: jdbc:postgresql://db:5432/myapp_dev
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: postgres
      JWT_ACCESS_SECRET: dev-access-secret-minimum-32-characters-long
      JWT_REFRESH_SECRET: dev-refresh-secret-minimum-32-characters-long
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:16-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: myapp_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
```

---

## 17. Checklist para o Agente de IA

Antes de gerar código, verificar **todos** os itens:

### Estrutura e Organização
```
[ ] Código organizado por feature (não por camada técnica)?
[ ] Cada feature tem suas próprias camadas (controller, service, repository, dto)?
[ ] Código compartilhado centralizado em common/?
[ ] Features não importam repositórios de outras features?
[ ] Um arquivo por classe, nomes seguindo convenções?
```

### Camadas
```
[ ] Controller recebe/retorna DTOs (nunca Entities)?
[ ] Controller não contém lógica de negócio?
[ ] Service contém toda a lógica de negócio?
[ ] Service lança exceções de domínio (não ResponseStatusException)?
[ ] Repository retorna Entities, não DTOs?
[ ] Injeção via construtor (não @Autowired em campo)?
```

### DTOs e Validação
```
[ ] DTOs implementados como Java Records?
[ ] DTOs separados para Create, Update e Response?
[ ] @Valid em todos os @RequestBody do Controller?
[ ] Validações com Bean Validation (@NotBlank, @Email, @Size)?
[ ] Campos sensíveis ausentes do DTO de Response?
[ ] Mapeamento via MapStruct (não manual)?
```

### API REST
```
[ ] URLs em kebab-case, plural?
[ ] Versionamento no path (/api/v1/)?
[ ] Status codes HTTP corretos (201, 204, 404, 422)?
[ ] Paginação em toda listagem (PageResponse)?
[ ] @Operation para documentação OpenAPI?
```

### Segurança
```
[ ] Senhas hasheadas com BCrypt (strength ≥ 12)?
[ ] JWT com expiração curta e claims validadas?
[ ] CORS com origens explícitas (sem wildcard)?
[ ] Endpoints protegidos com roles/authorities?
[ ] CSRF desabilitado apenas para API stateless?
[ ] Swagger desabilitado em produção?
```

### Banco de Dados
```
[ ] open-in-view: false?
[ ] ddl-auto: validate (não update/create)?
[ ] Migrations via Flyway (não ddl-auto)?
[ ] UUID como tipo de ID?
[ ] FetchType.LAZY em todos os relacionamentos?
[ ] N+1 mitigado (EntityGraph, JOIN FETCH, batch_fetch_size)?
[ ] Constraints nomeadas explicitamente?
```

### Exceções
```
[ ] GlobalExceptionHandler centraliza tratamento?
[ ] Exceções de domínio herdam de BusinessException?
[ ] Stack traces nunca expostos ao cliente?
[ ] Formato de erro padronizado (ErrorResponse)?
[ ] Erros de validação retornados por campo?
```

### Testes
```
[ ] Services testados com Mockito?
[ ] Controllers testados com MockMvc (@WebMvcTest)?
[ ] Repositories testados com @DataJpaTest?
[ ] Testcontainers para banco real?
[ ] Nomes descritivos (deve_ação_quandoCondição)?
[ ] AssertJ para assertions?
```

### Logging e Monitoramento
```
[ ] SLF4J + @Slf4j (nunca System.out)?
[ ] Request ID via MDC em todos os logs?
[ ] Dados sensíveis nunca logados?
[ ] Actuator habilitado com health/metrics?
[ ] Parâmetros com placeholder {} (sem concatenação)?
```

### Performance
```
[ ] OSIV desabilitado?
[ ] Cache em dados lidos frequentemente?
[ ] Operações pesadas assíncronas (@Async)?
[ ] HikariCP configurado adequadamente?
[ ] Compressão de resposta habilitada?
```

---

## Referências

| Recurso | URL |
|---------|-----|
| Spring Boot Reference | https://docs.spring.io/spring-boot/reference/ |
| Spring Security Reference | https://docs.spring.io/spring-security/reference/ |
| Spring Data JPA Reference | https://docs.spring.io/spring-data/jpa/reference/ |
| Hibernate Best Practices | https://vladmihalcea.com/ |
| MapStruct Reference | https://mapstruct.org/documentation/stable/reference/ |
| Flyway Documentation | https://documentation.red-gate.com/fd |
| Testcontainers Docs | https://testcontainers.com/ |
| SpringDoc OpenAPI | https://springdoc.org/ |
| OWASP API Security Top 10 | https://owasp.org/API-Security/ |
