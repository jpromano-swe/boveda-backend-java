# BOVEDA - Core Backend (SaaS Financiero)

Motor transaccional backend para **Bóveda**, una plataforma SaaS de automatización de ahorro e inversión basada en reglas de negocio. 

El sistema está diseñado para gestionar estrategias de inversión por usuario, procesar eventos de depósito de forma idempotente, ejecutar órdenes de mercado a través de integraciones con brokers externos (Binance, IOL) y mantener la reconciliación estricta de carteras.

---

## Arquitectura del Sistema

El proyecto implementa **Arquitectura Hexagonal (Ports & Adapters)** y principios de **Domain-Driven Design (DDD)** para garantizar el desacoplamiento absoluto entre la lógica de negocio core y las dependencias de infraestructura.

### Capas Principales

#### 1. Core Domain (Reglas de Negocio)
Aislado de cualquier framework externo. Implementa la lógica transaccional pura.
* **Entities:** `User`, `Strategy`, `Allocation`, `Trade`, `PortfolioSnapshot`, `CashReserve`.
* **Value Objects:** Modelado estricto para evitar errores de primitivas (ej. `Money` con escala fija y políticas de redondeo, `Percentage`, `InstrumentId`).
* **Casos de Uso Principales:** `ExecuteBuyUseCase`, `DetectDepositUseCase`, `ReconcileTradesUseCase`.

#### 2. Ports (Contratos)
* **Inbound Ports:** Interfaces que exponen los casos de uso hacia los adaptadores de entrada (REST Controllers).
* **Outbound Ports:** Contratos que el dominio utiliza para comunicarse con el exterior (`BrokerTradingPort`, `TradeRepository`, `SnapshotRepository`).

#### 3. Adapters (Infraestructura)
* **Inbound:** Controladores REST (`@RestController`) que orquestan los DTOs de entrada hacia comandos del dominio (`ExecuteBuyCommand`).
* **Outbound:** Implementaciones concretas de los puertos de salida (Adaptadores de Binance/IOL, repositorios PostgreSQL, workers para el patrón Outbox).

---

## Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework Core:** Spring Boot 3.3.x (Spring Web, Spring Data JDBC)
* **Base de Datos:** PostgreSQL
* **Migraciones:** Flyway
* **Integraciones:** RestClient (Spring 6+) con manejo de retries y timeouts.
* **Procesamiento Asíncrono:** Patrón Outbox en PostgreSQL + Schedulers distribuidos.
* **Testing:** JUnit 5, Mockito, Testcontainers (Postgres), WireMock (APIs externas).

---

## DevOps & CI/CD (Pipeline de Despliegue)

El ciclo de vida del código está automatizado para garantizar entregas continuas y seguras:

1. **Continuous Integration (CI):** Configurado mediante **GitHub Actions**. Cada Pull Request dispara la ejecución de la suite completa de tests unitarios y de integración (Testcontainers).
2. **Continuous Deployment (CD):** Los merges a la rama principal despliegan automáticamente los artefactos en el entorno PaaS de **Railway**.
3. **Gestión de Entorno:** Las variables de entorno, secretos y credenciales de bases de datos de producción son gestionadas y encriptadas de forma nativa en la plataforma de despliegue.

---

## Estándares de Ingeniería y Decisiones Técnicas

* **Test-Driven Development (TDD):** Cobertura rigurosa priorizando el flujo Red-Green-Refactor. Las reglas de dominio (ej. `FeePolicy`, `MinimumOrderPolicy`) se testean de forma aislada.
* **Idempotencia Transaccional:** Los endpoints críticos (como la detección de depósitos y la ejecución de compras) están diseñados para ser idempotentes, evitando la duplicación de operaciones financieras.
* **Manejo Monetario Seguro:** Prohibición estricta del uso de punto flotante (`double`/`float`) para transacciones. Se utiliza un Value Object `Money` encapsulando `BigDecimal`.
* **Capa Anticorrupción (ACL):** El dominio no consume payloads crudos de los brokers. Los adaptadores de infraestructura se encargan de la traducción hacia los modelos internos.
