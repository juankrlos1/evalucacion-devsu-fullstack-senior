# Banking Microservices Platform

**Author:** Juan Carlos De La Cruz Perez — Devsu

Sistema bancario basado en microservicios con arquitectura hexagonal, gateway reactivo y frontend Angular.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| **Backend** | Java 17, Spring Boot 3.3.5, Spring WebFlux, Spring Data JPA |
| **Gateway** | Spring Cloud Gateway 2023.0.3, Resilience4j, Caffeine |
| **Frontend** | Angular 21.2, TypeScript 5.9, SCSS, Vite |
| **Base de datos** | PostgreSQL 16 |
| **Mensajería** | RabbitMQ 3 (AMQP) |
| **Build** | Gradle 8.10.2, npm 11.12 |
| **Contenedores** | Docker multi-stage (eclipse-temurin:17) |
| **Tests** | JUnit 5, Karate 1.4.1, Jest 30, Testing Library |
| **Otros** | MapStruct 1.6.3, Lombok, OpenPDF 2.0.3, Springdoc OpenAPI |

---

## Arquitectura

```
┌──────────┐      ┌─────────────────┐      ┌──────────────────┐
│ Frontend │─────▶│   API Gateway   │─────▶│  bp-ms-customer  │
│ :4200    │      │   :8080         │      │  :8081           │
└──────────┘      │                 │      └────────┬─────────┘
                  │  Circuit Breaker│               │ RabbitMQ
                  │  Rate Limiting  │               ▼
                  │  Security       │      ┌──────────────────┐
                  │                 │─────▶│  bp-ms-account   │
                  └─────────────────┘      │  :8082           │
                                           └────────┬─────────┘
                                                    │
                  ┌──────────────────┐              │
                  │  PostgreSQL :5432│◀─────────────┘
                  │  RabbitMQ  :5672│
                  └──────────────────┘
```

### Hexagonal (Puertos y Adaptadores)

Cada microservicio sigue esta estructura:

```
com.banking.<service>/
├── application/          # Casos de uso, DTOs, mappers, puertos de entrada
├── domain/               # Modelos, excepciones, puertos de salida
└── infrastructure/       # Controladores REST, JPA, RabbitMQ, reportes
```

- **application/port** → Interfaces de entrada (casos de uso)
- **domain/port** → Interfaces de salida (repositorios)
- **infrastructure/** → Implementaciones concretas (adaptadores)

---

## Servicios

### Gateway (`:8080`)

| Ruta | Destino |
|------|---------|
| `/api/clientes/**` | bp-ms-customer:8081 |
| `/api/cuentas/**` | bp-ms-account:8082 |
| `/api/movimientos/**` | bp-ms-account:8082 |
| `/api/reportes/**` | bp-ms-account:8082 |

- Circuit breaker: ventana de 10 llamadas, umbral de fallo 50%, espera 10s en abierto
- Rate limiting: 100 requests/60s por IP (Caffeine)
- Fallback unificado en `/fallback/unavailable`

### Customer Service (`:8081`)

CRUD de clientes con eventos de dominio vía RabbitMQ (`customer.events` exchange).

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/clientes` | Listar clientes activos |
| GET | `/api/clientes/{id}` | Obtener por ID |
| POST | `/api/clientes` | Crear (ID auto-generado `CLI-xxxx`) |
| PUT | `/api/clientes/{id}` | Reemplazar |
| PATCH | `/api/clientes/{id}` | Actualización parcial / toggle activo |
| DELETE | `/api/clientes/{id}` | Desactivar (soft delete) |

### Account Service (`:8082`)

Cuentas, movimientos y reportes financieros.

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/cuentas` | Listar cuentas |
| POST | `/api/cuentas` | Crear (número auto-generado 6 dígitos) |
| PUT | `/api/cuentas/{num}` | Reemplazar |
| PATCH | `/api/cuentas/{num}` | Actualización parcial |
| DELETE | `/api/cuentas/{num}` | Desactivar |
| GET | `/api/movimientos` | Listar movimientos |
| POST | `/api/movimientos` | Crear movimiento |
| PUT | `/api/movimientos/{id}` | Reemplazar movimiento |
| DELETE | `/api/movimientos/{id}` | Eliminar (revierte saldo) |
| GET | `/api/reportes?customerId=X&startDate=Y&endDate=Z` | Estado de cuenta JSON |
| GET | `/api/reportes/pdf?customerId=X&startDate=Y&endDate=Z` | Estado de cuenta PDF (base64) |

**Regla de negocio:** Límite diario de retiro = $1,000 (configurable vía `DAILY_WITHDRAWAL_LIMIT`).

---

## Levantar el Ambiente

### Prerrequisitos

- Docker Desktop (con Docker Compose v2)
- Node.js ≥ 20 + npm ≥ 11 (para el frontend)
- Git

### Backend (Docker)

```bash
cd backend
docker compose up -d --build
```

Esto levanta **5 contenedores** con healthchecks:

| Contenedor | Puerto | Healthcheck |
|-----------|--------|-------------|
| bp-banking-db | 5432 | `pg_isready` |
| bp-rabbitmq | 5672, 15672 | `rabbitmq-diagnostics ping` |
| bp-ms-customer | 8081 | `/actuator/health` |
| bp-ms-account | 8082 | `/actuator/health` |
| bp-ms-gateway | 8080 | `/actuator/health` |

La base de datos se inicializa automáticamente con `BaseDatos.sql` (schemas, tablas y datos de prueba).

**Verificar que todo está sano:**

```bash
docker compose ps
# Todos deben mostrar "healthy"
```

### Frontend

```bash
cd frontend
npm install
npm start
# → http://localhost:4200
```

El frontend apunta al gateway en `http://localhost:8080/api`.

---

## Base de Datos

PostgreSQL con **2 schemas** en una sola base `bp_banking_db`:

- `customer_service` → tabla `customers`
- `account_service` → tablas `accounts`, `movements`

Credenciales por defecto: `banking` / `banking`

Hibernate configurado con `ddl-auto: none` — el DDL se maneja en `BaseDatos.sql`.

### Datos Precargados

- 3 clientes (Jose Lema, Marianela Montalvo, Juan Osorio)
- 5 cuentas (Savings y Checking)
- 4 movimientos iniciales

---

## Desarrollo Local (sin Docker)

Si necesitas correr los microservicios fuera de Docker:

1. Tener PostgreSQL en `localhost:5432` con base `bp_banking_db` y usuario `banking/banking`
2. Tener RabbitMQ en `localhost:5672` (usuario `guest/guest`)
3. Ejecutar el SQL de inicialización: `psql -U banking -d bp_banking_db -f BaseDatos.sql`
4. Desde `backend/`:

```bash
./gradlew :bp-ms-customer:bootRun
./gradlew :bp-ms-account:bootRun
./gradlew :bp-ms-gateway:bootRun
```

---

## Tests

### Backend — Unit Tests

```bash
cd backend
./gradlew test
```

### Backend — Integration Tests (Karate)

```bash
cd backend
./gradlew :bp-ms-account:integrationTest
```

Requiere servicios levantados. Los reportes se generan en `bp-ms-account/build/karate-reports/`.

### Frontend — Jest

```bash
cd frontend
npm test
```

---

## Mensajería (RabbitMQ)

- **Exchange:** `customer.events` (topic)
- **Routing Key:** `customer.events`
- **Cola:** `account.customer.events` (consumida por account service)

Flujo: Customer Service publica eventos (crear/actualizar/eliminar) → Account Service sincroniza datos del cliente localmente.

**Management UI:** http://localhost:15672 (guest/guest)

---

## API Docs (Swagger)

- Customer: http://localhost:8081/webjars/swagger-ui/index.html
- Account: http://localhost:8082/webjars/swagger-ui/index.html

Accesibles también a través del gateway en los puertos directos de cada servicio.

---

## Postman

Importar `backend/BankingMicroservices.postman_collection.json` en Postman. Contiene flujos completos de:
- CRUD clientes, cuentas, movimientos
- Reportes (JSON y PDF)
- Validaciones de reglas de negocio

---

## Variables de Entorno

| Variable | Default | Servicio |
|----------|---------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/bp_banking_db?currentSchema=<schema>` | customer, account |
| `SPRING_DATASOURCE_USERNAME` | `banking` | customer, account |
| `SPRING_DATASOURCE_PASSWORD` | `banking` | customer, account |
| `RABBITMQ_HOST` | `localhost` | customer, account |
| `RABBITMQ_PORT` | `5672` | customer, account |
| `RABBITMQ_USER` | `guest` | customer, account |
| `RABBITMQ_PASS` | `guest` | customer, account |
| `CUSTOMER_EVENTS_EXCHANGE` | `customer.events` | customer, account |
| `CUSTOMER_EVENTS_ROUTING_KEY` | `customer.events` | customer, account |
| `CUSTOMER_EVENTS_QUEUE` | `account.customer.events` | account |
| `DAILY_WITHDRAWAL_LIMIT` | `1000` | account |

---

## Logging

Formato: `yyyy-MM-dd HH:mm:ss.SSS [thread] LEVEL logger - message`

Niveles configurados:
- `com.banking.*`: INFO
- `com.banking.*.application`: DEBUG
- `org.springframework.amqp`: WARN

Logs incluyen trazabilidad de operaciones CRUD, circuit breaker fallbacks y validaciones de reglas de negocio.

```bash
# Ver logs de un servicio
docker logs bp-ms-customer --tail 50 -f
docker logs bp-ms-account --tail 50 -f
docker logs bp-ms-gateway --tail 50 -f
```

---

## Comandos Útiles

```bash
# Rebuild y redeploy un solo servicio
docker compose up -d --build bp-ms-customer

# Reiniciar todo
docker compose down && docker compose up -d --build

# Limpiar volúmenes (reset DB)
docker compose down -v

# Compilar sin Docker
cd backend && ./gradlew compileJava

# Generar JARs
cd backend && ./gradlew bootJar -x test
```
