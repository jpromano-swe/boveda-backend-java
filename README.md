# Bóveda Backend - Arquitectura y Setup Inicial

Backend para producto fintech **Bóveda**, orientado a:
- estrategias por usuario
- detección de depósitos
- ejecución de órdenes (Binance / IOL)
- reconciliación de operaciones
- snapshots para dashboard / gráficas

## Objetivo del proyecto
Migrar/reconstruir desde un prototipo hacia un backend robusto, mantenible y testeable, con foco en:
- reglas de negocio explícitas
- integraciones externas desacopladas
- idempotencia
- trazabilidad
- TDD desde el inicio

---

## Arquitectura elegida

### Estilo
**Hexagonal (Ports & Adapters)**

### Capas principales

#### Core (Dominio + Casos de uso)
- **Entidades** (planeadas):
  - `User`
  - `Strategy`
  - `Allocation`
  - `DepositEvent`
  - `Trade`
  - `Instrument`
  - `PortfolioSnapshot`
  - `CashReserve`
- **Value Objects**:
  - `Money (ARS)`
  - `Percent`
  - `InstrumentId`
  - `BrokerOrderId`
- **Casos de uso** (planeados):
  - `DetectDeposit`
  - `ExecuteBuy`
  - `BuildChartSeries`
  - `FreezeMonthlyStrategy`
  - `ReconcileTrades`

#### Ports (interfaces)
- **Outbound ports** (hacia infra / terceros):
  - `BrokerMarketDataPort`
  - `BrokerTradingPort`
  - `ExchangeBalancePort`
  - `DepositEventRepository`
  - `TradeRepository`
  - `StrategyRepository`
  - `SnapshotRepository`
  - `JobQueuePort`
- **Inbound ports** (entrada al core):
  - interfaces de casos de uso consumidas por REST / jobs

#### Adapters
- **Inbound**
  - REST Controllers
- **Outbound**
  - Binance Adapter
  - IOL Adapter
  - Postgres Repositories
  - Job Queue / Outbox Worker

#### Infra
- configuración Spring
- seguridad
- observabilidad
- scheduling
- migraciones DB
- CI/CD
- manejo de secretos

---

## Stack elegido (actual)

### Runtime / Framework
- **Java 21**
- **Spring Boot 3.3.x**
- **Spring Web**
- **Spring Data JDBC** (elegido sobre JPA para mayor simplicidad y control)

### Base de datos
- **PostgreSQL**
- **Flyway** para migraciones

### Integraciones HTTP
- **RestClient (Spring 6+)**
  - decisión pragmática para arrancar
  - suficiente para volumen inicial esperado
  - menor complejidad que WebClient/reactive

### Jobs / Async (decisión recomendada para MVP)
- **Outbox pattern en Postgres + worker scheduler**
- `@Scheduled` + **ShedLock** (cuando se implemente scheduler multi-instancia)

### Testing (TDD-first)
- **JUnit 5**
- **Mockito**
- **Testcontainers (Postgres)**
- **WireMock** (APIs externas)

### Observabilidad (planificado)
- **Spring Boot Actuator**
- **Micrometer + Prometheus**
- logs estructurados con `correlationId`

---

## Decisiones técnicas importantes (MVP)

### 1) TDD como regla de trabajo
Se implementa siguiendo:
1. **Red** (test falla)
2. **Green** (mínimo código para pasar)
3. **Refactor**

### 2) Dinero con `BigDecimal`
- Nunca usar `double` para montos
- `Money` con escala fija y rounding explícito
- Reglas monetarias testeadas antes de usar en casos de uso

### 3) Idempotencia (requisito)
Casos críticos deberán ser idempotentes:
- `DetectDeposit`
- `ExecuteBuy`
- `ReconcileTrades`

### 4) Anti-corruption layer para brokers
El dominio no debe depender de payloads crudos de Binance/IOL.

---

## Estructura de paquetes (actual)

```text
org.boveda.backend
├── domain
│   ├── model
│   ├── vo
│   ├── service
│   ├── event
│   └── exception
├── application
│   ├── usecase
│   ├── service
│   ├── command
│   ├── query
│   └── dto
├── ports
│   ├── in
│   └── out
├── adapters
│   ├── in
│   │   └── rest
│   └── out
│       ├── broker
│       │   ├── binance
│       │   └── iol
│       ├── persistence
│       │   └── postgres
│       └── jobs
│           └── outbox
└── infra
    ├── config
    ├── security
    ├── observability
    └── scheduling

```
## Documentación técnica
- [Orden de reglas de compra](docs/technical/order-of-rules.md)


Los ports no “procesan”

Son interfaces/contratos, no lógica.
Definen cómo se comunican capas.
El JSON no entra por ports directamente

Entra por un adapter inbound (ej. controller REST).
Ese adapter transforma JSON -> ExecuteBuyCommand.
Luego llama al inbound port (ExecuteBuyUseCase).
Modelo correcto:

REST Controller (adapter inbound) recibe JSON.
Mapea JSON a ExecuteBuyCommand.
Llama ExecuteBuyUseCase.execute(command) (puerto de entrada).
ExecuteBuyService implementa ese puerto y aplica reglas de dominio.
Para salir al broker, usa BrokerTradingPort (puerto de salida, interfaz).
Un adapter outbound real (Binance/IOL) implementará BrokerTradingPort.
El use case devuelve ExecuteBuyResult (DTO de salida para app/API).
Resumen rápido de cada pieza:

ExecuteBuyUseCase: contrato de entrada del caso de uso.
ExecuteBuyService: implementación con lógica de aplicación.
ExecuteBuyCommand: datos de entrada del caso de uso.
ExecuteBuyResult: datos de salida del caso de uso.
BrokerTradingPort: contrato de salida hacia broker.
Binance/IOL Adapter: implementación concreta del port de salida.
