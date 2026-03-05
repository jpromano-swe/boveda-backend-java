# API de depósitos (ingesta + lectura)

- Fecha: 2026-03-05
- Estado: Completado

## Objetivo
Cerrar el flujo API de depósitos:
1. Ingesta de eventos (`detect`)
2. Lectura para dashboard (`list`)

## Alcance implementado

### 1) Ingesta de depósitos
- Endpoint: `POST /api/v1/deposits/detect`
- Controller: `DetectDepositController`
- Request/Response DTOs:
  - `DetectDepositRequest`
  - `DetectDepositResponse`
- Validación de payload en capa API (`@Valid`)
- Mapeo de errores a HTTP 400 con `ApiExceptionHandler`
- Resultado esperado:
  - `201 CREATED` cuando el evento es nuevo
  - `200 OK` cuando es duplicado

### 2) Lectura de depósitos para dashboard
- Endpoint: `GET /api/v1/users/{userId}/deposits`
- Query params:
  - `from` (opcional, ISO-8601)
  - `to` (opcional, ISO-8601)
  - `limit` (default 50)
- Caso de uso:
  - `ListDepositsUseCase`
  - `ListDepositsService`
- Port de salida:
  - `DepositQueryRepository`
- Adapter JDBC:
  - `PostgresDepositQueryRepository`
- Reglas:
  - `userId` obligatorio
  - `limit` entre 1 y 500
  - `from <= to` cuando ambos existen
- Orden de salida:
  - `occurred_at DESC`

## Persistencia
- Tabla usada: `deposit_events`
- Índices relevantes:
  - único por (`source`, `external_event_id`)
  - búsqueda por (`user_id`, `occurred_at desc`)

## Testing validado
- Web tests:
  - `DetecDepositControllerTest`
  - `ListDepositsControllerTest`
- Unit test:
  - `ListDepositsServiceTest`
- Integration test local:
  - `PostgresDepositQueryRepositoryLocalIT`

## Decisiones técnicas
- Se priorizó avanzar con IT local (`it-local`) para no bloquear sprint por compatibilidad local de Testcontainers/Docker.
- El diseño mantiene arquitectura hexagonal: controllers -> use cases -> ports -> adapters JDBC.
