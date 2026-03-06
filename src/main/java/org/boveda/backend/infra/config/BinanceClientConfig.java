package org.boveda.backend.infra.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(BinanceProperties.class)
public class BinanceClientConfig {

  @Bean(name = "binanceRestClient")
  public RestClient binanceRestClient(RestClient.Builder builder, BinanceProperties properties) {
    HttpClient httpClient = HttpClient.newBuilder()
      .connectTimeout(properties.connectTimeout())
      .build();

    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.readTimeout());

    return builder
      .baseUrl(properties.baseUrl())
      .requestFactory(requestFactory)
      .build();
  }

  @Bean(name = "binanceTradingRetry")
  public Retry binanceTradingRetry(BinanceProperties properties) {
    BinanceProperties.RetryProps r = properties.resilience().retry();
    RetryConfig config = RetryConfig.custom()
      .maxAttempts(r.maxAttempts())
      .waitDuration(r.waitDuration())
      .build();
    return Retry.of("binance-trading-retry", config);
  }

  @Bean(name = "binanceTradingCircuitBreaker")
  public CircuitBreaker binanceTradingCircuitBreaker(BinanceProperties properties) {
    BinanceProperties.CircuitBreakerProps c = properties.resilience().circuitBreaker();
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
      .minimumNumberOfCalls(c.minimumNumberOfCalls())
      .slidingWindowSize(c.slidingWindowSize())
      .failureRateThreshold(c.failureRateThreshold())
      .waitDurationInOpenState(c.waitDurationInOpenState())
      .build();
    return CircuitBreaker.of("binance-trading-cb", config);
  }

  @Bean(name = "binanceMarketDataRetry")
  public Retry binanceMarketDataRetry(BinanceProperties properties) {
    BinanceProperties.RetryProps r = properties.resilience().retry();
    RetryConfig config = RetryConfig.custom()
      .maxAttempts(r.maxAttempts())
      .waitDuration(r.waitDuration())
      .build();
    return Retry.of("binance-marketdata-retry", config);
  }

  @Bean(name = "binanceMarketDataCircuitBreaker")
  public CircuitBreaker binanceMarketDataCircuitBreaker(BinanceProperties properties) {
    BinanceProperties.CircuitBreakerProps c = properties.resilience().circuitBreaker();
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
      .minimumNumberOfCalls(c.minimumNumberOfCalls())
      .slidingWindowSize(c.slidingWindowSize())
      .failureRateThreshold(c.failureRateThreshold())
      .waitDurationInOpenState(c.waitDurationInOpenState())
      .build();
    return CircuitBreaker.of("binance-marketdata-cb", config);
  }
}
