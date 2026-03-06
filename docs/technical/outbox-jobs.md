# Outbox pattern + JobQueue + Scheduler

- Fecha: 2026-03-06
- Estado: Completado

## Objetivo
Implementar infraestructura de jobs confiable usando Outbox pattern para desacoplar eventos de negocio del procesamiento asíncrono.

## Alcance implementado

### 1) Esquema de outbox (DB)
- Migración: `V2__create_outbox.sql`
- Tabla `outbox` con campos:
  - `id`
  - `event_type`
  - `aggregate_type`
  - `aggregate_id`
  - `payload` (jsonb)
  - `status`
  - `attempts`
  - `next_retry_at`
  - `last_error`
  - `created_at`
  - `updated_at`
- Índices:
  - `ix_outbox_status_retry_created` para polling eficiente por estado/ventana
  - `ix_outbox_aggregate` para trazabilidad por agregado

### 2) Modelo de dominio y repositorio
- `OutboxStatus`: `PENDING`, `PROCESSING`, `RETRY`, `SENT`, `DEAD`
- `OutboxEvent` con invariantes de integridad
- Port `OutboxRepository`:
  - `save`
  - `findDispatchable`
  - `markProcessing`
  - `markSent`
  - `markRetry`
  - `markDead`
- Adapter JDBC:
  - `PostgresOutboxRepository`

### 3) JobQueuePort sobre outbox
- Nuevo port `JobQueuePort` para encolar eventos de forma abstracta
- Adapter `OutboxJobQueueAdapter` que persiste en `outbox`
- Integración en `DetectDepositService`:
  - al crear depósito nuevo, se encola evento `DepositDetected`

### 4) Worker de despacho
- Servicio `DispatchOutboxService`:
  - toma batch de eventos despachables
  - intenta lock lógico con `markProcessing`
  - ejecuta `OutboxEventDispatcher`
  - marca `SENT` en éxito
  - marca `RETRY` con backoff en error temporal
  - marca `DEAD` al superar `maxAttempts`
- Scheduler:
  - `OutboxDispatchScheduler` con `@Scheduled`
- Dispatcher inicial:
  - `NoopOutboxEventDispatcher` (placeholder controlado para MVP)

### 5) Tests
- `PostgresOutboxRepositoryLocalIT`:
  - save + query dispatchable + markSent + markRetry
- `DispatchOutboxServiceTest`:
  - éxito -> sent
  - fallo temporal -> retry
  - max attempts -> dead
  - lock fallido -> skipped
- `./mvnw test` en verde

## Decisiones
- Se mantuvo implementación con Spring Data JDBC/JdbcTemplate para control explícito.
- El dispatcher concreto de eventos externos queda desacoplado y evoluciona en próximos días (Binance/IOL).
- Se priorizó robustez de estado y reintentos antes de integrar handlers reales.
