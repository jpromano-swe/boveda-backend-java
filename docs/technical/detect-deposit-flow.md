# Flujo técnico de DetectDeposit

- Fecha: 2026-03-04
- Estado: Aprobado (MVP)
- Contexto: detección de depósitos desde brokers/exchanges

## Objetivo
Registrar eventos de depósito de forma idempotente y trazable.

## Contrato de entrada
Caso de uso: `DetectDepositUseCase`  
Comando: `DetectDepositCommand`

Campos:
- `userId`
- `source`
- `externalEventId`
- `amount`
- `occurredAt`
- `correlationId` (opcional)

## Reglas aplicadas
1. Validar que el comando no sea nulo.
2. Validar que `userId` no sea nulo.
3. Validar que `source` no esté vacío.
4. Validar que `externalEventId` no esté vacío.
5. Validar que `amount` sea positivo.
6. Validar que `occurredAt` no sea nulo.

Si `correlationId` no viene o viene en blanco:
- Se genera un UUID nuevo.

## Detección de duplicados
Antes de persistir:
- `source` se normaliza a mayúsculas.
- `externalEventId` se evalúa trim.

Se consulta:
- `existsBySourceAndExternalEventId(normalizedSource, normalizedExternalEventId)`

Si ya existe:
- Resultado `DUPLICATE`.
- No se persiste un nuevo evento.

Si no existe:
- Se crea `DepositEvent`.
- Resultado `CREATED`.

## Campos de auditoría
Al crear el evento:
- `detectedAt = Instant.now()`
- `createdAt = Instant.now()`
- `updatedAt = Instant.now()`
- `correlationId` (recibido o generado)

## Cobertura de tests (actual)
`DetectDepositServiceTest` cubre:
- creación cuando no hay duplicado
- resultado `DUPLICATE` cuando ya existe
- rechazo de `source` en blanco
- rechazo de `externalEventId` en blanco
- rechazo de monto no positivo
- rechazo de comando nulo
