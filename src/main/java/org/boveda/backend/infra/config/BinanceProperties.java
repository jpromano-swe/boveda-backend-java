package org.boveda.backend.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "binance")
public record BinanceProperties(
  String baseUrl,
  Duration connectTimeout,
  Duration readTimeout
) {}
