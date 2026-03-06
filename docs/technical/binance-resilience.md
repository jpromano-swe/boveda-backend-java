# Binance adapters con resiliencia (Retry + CircuitBreaker)

## Objetivo
Hacer más robustas las integraciones Binance (market data y trading) frente a fallas transitorias y proteger al sistema de cascadas de error.

## Cambios aplicados
- `BinanceTradingAdapter`:
  - Decoración de llamada HTTP con `Retry` y `CircuitBreaker`.
  - Manejo explícito de `CallNotPermittedException` cuando el circuito está abierto.
  - Conservación de `IntegrationException` de dominio y mapeo consistente para errores HTTP/timeout.
- `BinanceMarketDataAdapter`:
  - Misma estrategia de resiliencia que trading.
- Tests WireMock:
  - Caso retry: primer 500 y segundo 200.
  - Caso circuit breaker: múltiples fallos y tercera llamada bloqueada localmente.

## Decisiones de diseño
- Se mantuvieron constructores “runtime” y constructores “testables”.
- Runtime usa defaults de Resilience4j.
- Tests inyectan `Retry`/`CircuitBreaker` con configuración corta para validar comportamiento de manera determinística.

## Riesgos / próximos pasos
- Externalizar configuración de retry/circuit breaker a `application.yaml` para producción.
- Definir política por endpoint:
  - market data: retry agresivo tolerable.
  - trading: retry más conservador para evitar efectos duplicados no idempotentes.
- Instrumentar métricas de resiliencia (retries, estado de circuit).
