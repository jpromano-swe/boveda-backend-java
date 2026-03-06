
# Foundation - Trade aggregate + ReconcileTrades use case

- Fecha: 2026-03-06
- Estado: Completado (base funcional)
- Nota: Este trabajo adelanta scope previsto originalmente para Día 10.

## Objetivo
Construir la base de reconciliación de órdenes:
- agregado `Trade` con transiciones de estado válidas
- caso de uso `ReconcileTrades` sobre puertos

## Cambios implementados

1. Modelo de dominio:
- `TradeStatus` (`PENDING`, `FILLED`, `CANCELED`, `REJECTED`)
- `Trade` con invariantes y transiciones:
  - `markFilled(now)`
  - `markCanceled(now)`
  - `markRejected(now)`
- Se bloquean transiciones desde estados finales (`BusinessRuleViolationException`)

2. Caso de uso:
- `ReconcileTradesService` + `ReconcileTradesUseCase`
- Entrada: `ReconcileTradesCommand(batchSize, now)`
- Salida: `ReconcileTradesResult(reconciledCount, unchangedCount)`
- Reglas:
  - `batchSize > 0`
  - `now != null`
  - si broker responde `PENDING`, no hay cambios
  - si broker responde estado final, se persiste transición

3. Puertos:
- `TradeRepository` (`findPendingTrades`, `save`)
- `ExchangeOrderStatusPort` (`fetchStatus` + enum `OrderStatus`)

## Cobertura de tests
- `TradeTest`
- `ReconcileTradesServiceTest`
- casos de borde: batch inválido y `now` nulo

## Riesgos / pendientes
- Falta persistencia JDBC de `Trade`.
- Falta endpoint/scheduler que invoque reconciliación.
- Falta auditoría de transiciones en DB.
