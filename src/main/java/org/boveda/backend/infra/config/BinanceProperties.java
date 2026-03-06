package org.boveda.backend.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "binance")
public record BinanceProperties(
  String baseUrl,
  Duration connectTimeout,
  Duration readTimeout,
  Resilience resilience
) {
  public record Resilience(
    RetryProps retry,
    CircuitBreakerProps circuitBreaker
  ) {}

  public record RetryProps(
    int maxAttempts,
    Duration waitDuration
  ) {}

  public record CircuitBreakerProps(
    int minimumNumberOfCalls,
    int slidingWindowSize,
    float failureRateThreshold,
    Duration waitDurationInOpenState
  ) {}
}
