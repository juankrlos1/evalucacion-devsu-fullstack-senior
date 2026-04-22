# Banking Microservices

Java 17 / Spring Boot 3.3.5 / WebFlux reactive microservices for banking operations.
The repository is organized as a Gradle multi-module project with two services:
**bp-ms-customer** and **bp-ms-account**.

## Technology Stack

- Java 17, Spring Boot 3.3.5, Spring WebFlux (reactive)
- Spring Data JPA (with reactive wrappers via `Schedulers.boundedElastic()`)
- Spring Boot Actuator (health checks)
- Bean Validation (Jakarta)
- PostgreSQL 16, RabbitMQ 3
- Lombok, MapStruct
- OpenPDF (PDF report generation)
- JUnit 5, Mockito, AssertJ, WebTestClient, H2 (unit tests)
- Karate Framework (integration tests)
- Docker & Docker Compose

## Project Structure

```
├── settings.gradle.kts
├── build.gradle.kts                    # Root build with shared dependencies
├── bp-ms-customer/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/banking/customer/
│       │   ├── domain/                 (model, exception, port/out)
│       │   ├── application/            (port/in, usecase, dto, mapper, event)
│       │   └── infrastructure/         (web, persistence, messaging)
│       ├── test/                       (unit tests - JUnit 5 + WebTestClient)
│       └── integrationTest/            (Karate integration tests)
├── bp-ms-account/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/banking/account/
│       │   ├── domain/                 (model, exception, port/out)
│       │   ├── application/            (port/in, usecase, dto, mapper)
│       │   └── infrastructure/         (web, persistence, messaging)
│       ├── test/                       (unit tests - JUnit 5 + Mockito + WebTestClient)
│       └── integrationTest/            (Karate integration tests)
├── BaseDatos.sql
├── Dockerfile                          (multi-stage, non-root user)
├── docker-compose.yml                  (healthchecks, restart policies)
└── BankingMicroservices.postman_collection.json
```

## Architecture

Hexagonal Architecture (Ports & Adapters) per service:

```
domain (models, exceptions, output ports)
  ↑
application (input ports, use cases, DTOs, mappers)
  ↑
infrastructure (web adapters, persistence adapters, messaging adapters)
```

### Design Patterns

- **Repository Pattern** — Domain repository interfaces as output ports (SRP, DIP)
- **Adapter Pattern** — Persistence and messaging adapters implementing ports (OCP, LSP)
- **Strategy Pattern** — Report format (JSON vs PDF base64) via format parameter
- **Immutable Domain Models** — All domain objects are immutable; updates create new instances
- **Port & Adapter (Hexagonal)** — Clean separation between domain, application, and infrastructure layers
- **DTO Pattern** — Separate request/response DTOs from domain models
- **Global Exception Handler** — Centralized error handling via `@RestControllerAdvice`

### SOLID Principles

- **SRP** — Each class has a single responsibility (use cases, mappers, adapters, controllers)
- **OCP** — New features can be added without modifying existing code (new adapters, new report formats)
- **LSP** — Domain repository interfaces can be substituted with any implementation
- **ISP** — Fine-grained service interfaces (AccountService, MovementService, ReportService)
- **DIP** — Domain layer depends on abstractions (ports), not infrastructure details

## bp-ms-customer (Port 8081)

Manages customers (Person + Customer entities with inheritance).

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/clientes` | Create customer |
| GET | `/api/clientes` | List all customers |
| GET | `/api/clientes/{customerId}` | Get customer by ID |
| PUT | `/api/clientes/{customerId}` | Replace customer |
| PATCH | `/api/clientes/{customerId}` | Partial update |
| DELETE | `/api/clientes/{customerId}` | Deactivate (soft delete) |

### Example Request

```json
{
  "customerId": "CLI-001",
  "name": "Jose Lema",
  "gender": "Male",
  "age": 32,
  "identification": "0102030405",
  "address": "Otavalo sn y principal",
  "phone": "098254785",
  "password": "1234",
  "active": true
}
```

## bp-ms-account (Port 8082)

Manages accounts, movements, and reports.

### Account Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/cuentas` | Create account |
| GET | `/api/cuentas` | List all accounts |
| GET | `/api/cuentas/{accountNumber}` | Get account |
| PUT | `/api/cuentas/{accountNumber}` | Replace account |
| PATCH | `/api/cuentas/{accountNumber}` | Partial update |
| DELETE | `/api/cuentas/{accountNumber}` | Deactivate |

### Movement Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/movimientos` | Create movement |
| GET | `/api/movimientos` | List all movements |
| GET | `/api/movimientos/{movementId}` | Get movement |
| PUT | `/api/movimientos/{movementId}` | Replace movement |
| PATCH | `/api/movimientos/{movementId}` | Partial update |
| DELETE | `/api/movimientos/{movementId}` | Delete/reverse movement |

### Report Endpoint

```
GET /api/reportes?fecha={startDate},{endDate}&cliente={customerId}&format=json|pdf
```

- `format=json` (default): Returns JSON array of statement rows
- `format=pdf`: Returns base64-encoded PDF

### Business Rules

- Positive values = deposits, negative values = withdrawals
- Insufficient balance → `"Saldo no disponible"` (400)
- Daily withdrawal limit: $1000 → `"Cupo diario Excedido"` (400)
- Movements auto-calculate type from value sign

## Build & Test

### Backend Unit Tests

```bash
# Run all backend unit tests
./gradlew test

# Run tests for a specific module
./gradlew :bp-ms-customer:test
./gradlew :bp-ms-account:test
```

### Backend Integration Tests (Karate)

Karate integration tests run against the Docker-deployed services (ports 8081/8082 directly):

```bash
# Start services first
docker compose up --build -d

# Wait for services to be healthy, then run Karate integration tests
./gradlew :bp-ms-customer:integrationTest
./gradlew :bp-ms-account:integrationTest
```

Feature files:
- `clientes.feature` — Full CRUD lifecycle, validation, duplicates, 404 handling
- `cuentas.feature` — Account CRUD, duplicate detection, deactivation
- `movimientos.feature` — Deposits, withdrawals, insufficient balance, daily limit, delete/reverse
- `reportes.feature` — JSON and PDF report generation, date range filtering

### Frontend Unit Tests

```bash
cd ../frontend

# Install dependencies (first time)
npm install

# Run unit tests with Jest
npx jest

# Or using the Angular CLI
npx ng test
```

### Build All

```bash
# Build all backend modules
./gradlew build

# Build specific module
./gradlew :bp-ms-customer:build
./gradlew :bp-ms-account:build
```

### Unit Tests (Details)

The project includes comprehensive unit tests:

- **Domain model tests**: `CustomerTest`, `AccountTest`, `MovementTest` — validate business rules
- **Use case tests (Mockito)**: `CustomerUseCaseTest`, `AccountUseCaseTest`, `MovementUseCaseTest` — test application logic in isolation
- **Controller integration tests (WebTestClient)**: `CustomerControllerTest`, `AccountControllerTest`, `MovementControllerTest` — test HTTP layer with H2

## Authentication & Authorization

The system uses JWT-based authentication with role-based access control.

### Login

```
POST /api/auth/login
Content-Type: application/json

{ "identification": "ADMIN", "password": "1234" }
```

Returns a JWT token that must be sent as `Authorization: Bearer <token>` in all subsequent requests.

### Test Users

| User | Identification | Password | Role | Customer ID |
|------|---------------|----------|------|-------------|
| Juan Carlos De La Cruz | `ADMIN` | `1234` | ADMIN | CLI-ADMIN |
| Jose Lema | `0102030405` | `1234` | USER | CLI-001 |
| Marianela Montalvo | `0203040506` | `5678` | USER | CLI-002 |
| Juan Osorio | `0304050607` | `1245` | USER | CLI-003 |

### Roles & Permissions

| Feature | ADMIN | USER |
|---------|-------|------|
| Login | ✅ | ✅ |
| View/manage all customers | ✅ | ❌ |
| View all accounts | ✅ | Only own |
| View all movements | ✅ | Only own |
| Generate reports | ✅ Any client | Own only |
| Create accounts/movements | ✅ | ✅ |

### Password Security

- Passwords are stored as **BCrypt hashes** in the database (never plain text)
- On first startup, the `PasswordMigrator` component detects any plain-text passwords in the seed data and hashes them automatically
- Login validates credentials using `BCryptPasswordEncoder.matches()`
- Passwords are sent from the frontend to backend over HTTPS (in production); the backend handles all hashing

## Docker Deployment

```bash
docker compose up --build
```

### Docker Best Practices

- **Multi-stage build** — JDK for compilation, JRE for runtime (smaller image)
- **Non-root user** — Runs as `appuser` inside the container
- **Health checks** — All services have Docker-level healthchecks via `/actuator/health`
- **Restart policies** — `unless-stopped` for all services
- **Dependency ordering** — Account service waits for customer service to be healthy

Services:
- **bp-ms-customer**: `http://localhost:8081`
- **bp-ms-account**: `http://localhost:8082`
- **PostgreSQL**: `localhost:5432` (db: `bp_banking_db`)
- **RabbitMQ**: `localhost:5672` (Management UI: `http://localhost:15672`)

## Inter-Service Communication

Customer events are published to RabbitMQ when customers are created, updated, or deactivated.
The account service consumes these events via a `@RabbitListener`.

Event types: `CUSTOMER_CREATED`, `CUSTOMER_UPDATED`, `CUSTOMER_DEACTIVATED`

## Health Checks

Both services expose Spring Boot Actuator health endpoints:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## Notes

- The services expect PostgreSQL and RabbitMQ configuration through environment variables when running outside tests.
- Tests use H2 in PostgreSQL compatibility mode.
- `spring.jpa.hibernate.ddl-auto` is configured as `none` — schema is managed by `BaseDatos.sql`.
- The `BaseDatos.sql` script creates schemas, tables, indexes, and seed data for both services.
