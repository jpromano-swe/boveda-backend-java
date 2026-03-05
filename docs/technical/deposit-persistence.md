# Persistencia de DetectDeposit (Postgres + Flyway + JDBC)

- Fecha: 2026-03-05
- Estado: Completado

## Objetivo
Llevar `DetectDeposit` desde lógica en memoria a persistencia real en PostgreSQL.

## Cambios implementados
1. Migración Flyway:
- `src/main/resources/db/migration/V1__create_deposit_events.sql`
- Tabla `deposit_events`
- Índice único por (`source`, `external_event_id`)
- Índice por (`user_id`, `occurred_at`)

2. Repositorio JDBC:
- `src/main/java/org/boveda/backend/adapters/out/persistence/postgres/PostgresDepositEventRepository.java`
- Implementa `DepositEventRepository`
- Métodos:
  - `existsBySourceAndExternalEventId`
  - `save`

3. Ajuste de tipos temporales:
- En el `save`, los `Instant` se persisten como `Timestamp.from(...)` para compatibilidad con driver PostgreSQL.

4. Test de integración local:
- `src/test/java/org/boveda/backend/adapters/out/persistence/postgres/PostgresDepositEventRepositoryLocalIT.java`
- Perfil `it-local`
- Verifica:
  - guarda evento
  - detecta duplicado por (`source`, `external_event_id`)

5. Configuración de test local:
- `src/test/resources/application-it-local.yaml`
- datasource a `jdbc:postgresql://localhost:5432/boveda`

## Observaciones
- Testcontainers quedó pendiente por incompatibilidad Docker/Testcontainers en entorno local.
- Se continuó con IT local para no bloquear avance funcional del sprint.
