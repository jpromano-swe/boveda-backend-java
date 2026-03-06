package org.boveda.backend.adapters.out.broker.binance;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.boveda.backend.domain.exception.IntegrationException;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.ports.out.BrokerMarketDataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static org.junit.jupiter.api.Assertions.*;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


class BinanceMarketDataAdapterTest {

  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
    .options(options().dynamicPort())
    .build();

  private BrokerMarketDataPort adapter;

  @BeforeEach
  void setUp() {
    adapter = new BinanceMarketDataAdapter(
      newClient(),
      retry(1),
      circuitBreaker(10, 10)
    );
  }

  private RestClient newClient() {
    HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofMillis(300))
      .build();

    JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(httpClient);
    rf.setReadTimeout(Duration.ofMillis(300));

    return RestClient.builder()
      .baseUrl(wm.baseUrl())
      .requestFactory(rf)
      .build();
  }

  private Retry retry(int maxAttempts) {
    RetryConfig config = RetryConfig.custom()
      .maxAttempts(maxAttempts)
      .waitDuration(Duration.ofMillis(10))
      .retryOnException(this::isRetryable)
      .build();
    return Retry.of("binance-md-retry-" + maxAttempts, config);
  }

  private CircuitBreaker circuitBreaker(int minCalls, int windowSize) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
      .minimumNumberOfCalls(minCalls)
      .slidingWindowSize(windowSize)
      .failureRateThreshold(50.0f)
      .waitDurationInOpenState(Duration.ofSeconds(30))
      .recordException(this::isRetryable)
      .build();
    return CircuitBreaker.of("binance-md-cb-" + minCalls + "-" + windowSize, config);
  }

  private boolean isRetryable(Throwable ex) {
    if (ex instanceof ResourceAccessException) {
      return true;
    }
    if (ex instanceof RestClientResponseException responseException) {
      int status = responseException.getStatusCode().value();
      return status == 429 || status >= 500;
    }
    return false;
  }

  @Test
  void returnsPriceOn200() {
    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .withQueryParam("symbol", equalTo("BTCUSDT"))
      .willReturn(okJson("{\"symbol\":\"BTCUSDT\",\"price\":\"68250.12\"}")));

    BrokerMarketDataPort.BrokerQuote quote = adapter.fetchPrice(InstrumentId.of("BTCUSDT"));

    assertEquals(new BigDecimal("68250.12"), quote.price());
    assertEquals("BINANCE", quote.source());
  }

  @Test
  void throwsOn429() {
    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .willReturn(aResponse().withStatus(429).withBody("rate limited")));

    assertThrows(IntegrationException.class, () -> adapter.fetchPrice(InstrumentId.of("BTCUSDT")));
  }

  @Test
  void throwsOn500() {
    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .willReturn(serverError()));

    assertThrows(IntegrationException.class, () -> adapter.fetchPrice(InstrumentId.of("BTCUSDT")));
  }

  @Test
  void throwsOnTimeout() {
    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .willReturn(aResponse().withStatus(200).withFixedDelay(1000).withBody("{\"symbol\":\"BTCUSDT\",\"price\":\"1\"}")));

    assertThrows(IntegrationException.class, () -> adapter.fetchPrice(InstrumentId.of("BTCUSDT")));
  }

  @Test
  void retriesOnceOn500ThenSucceeds() {
    adapter = new BinanceMarketDataAdapter(
      newClient(),
      retry(2),
      circuitBreaker(10, 10)
    );

    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .inScenario("retry-quote")
      .whenScenarioStateIs(STARTED)
      .willReturn(serverError())
      .willSetStateTo("SECOND_CALL"));

    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .inScenario("retry-quote")
      .whenScenarioStateIs("SECOND_CALL")
      .willReturn(okJson("{\"symbol\":\"BTCUSDT\",\"price\":\"70000.00\"}")));

    BrokerMarketDataPort.BrokerQuote quote = adapter.fetchPrice(InstrumentId.of("BTCUSDT"));

    assertEquals(new BigDecimal("70000.00"), quote.price());
    wm.verify(2, getRequestedFor(urlPathEqualTo("/api/v3/ticker/price")));
  }

  @Test
  void opensCircuitAfterConsecutiveFailures() {
    adapter = new BinanceMarketDataAdapter(
      newClient(),
      retry(1),
      circuitBreaker(2, 2)
    );

    wm.stubFor(get(urlPathEqualTo("/api/v3/ticker/price"))
      .willReturn(serverError()));

    assertThrows(IntegrationException.class, () -> adapter.fetchPrice(InstrumentId.of("BTCUSDT")));
    assertThrows(IntegrationException.class, () -> adapter.fetchPrice(InstrumentId.of("BTCUSDT")));

    IntegrationException ex =
      assertThrows(IntegrationException.class, () -> adapter.fetchPrice(InstrumentId.of("BTCUSDT")));

    assertInstanceOf(CallNotPermittedException.class, ex.getCause());
    wm.verify(2, getRequestedFor(urlPathEqualTo("/api/v3/ticker/price")));
  }

}
