# Orden de aplicación de reglas de compra

- Fecha: 2026-03-02
- Estado: Aprobado (MVP)
- Contexto: Flujo de compra en Bóveda (dominio)

## Decisión
El orden de aplicación será:

1. Validar mínimo de orden (`MinimumOrderPolicy`)
2. Calcular fee (`FeePolicy`)
3. Calcular y validar remanente (`RemainderPolicy`)

## Justificación
- Evita procesar operaciones que ya nacen inválidas por mínimo.
- Hace explícito el costo antes del monto neto final.
- Garantiza que no se termine con remanente negativo.

## Ejemplo
- Monto bruto: ARS 1,000.00
- Fee (2.50%): ARS 25.00
- Neto: ARS 975.00
- Remanente: bruto - usado (debe ser >= 0)

## Impacto
- Los casos de uso de compra deben respetar este orden.
- Los tests de aplicación deben validar explícitamente esta secuencia.
