package org.boveda.backend.adapters.out.broker.binance;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.boveda.backend.domain.exception.IntegrationException;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.BrokerTradingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BinanceTradingAdapterTest {

  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
    .options(options().dynamicPort())
    .build();

  @BeforeEach
  void setUp() {
    adapter = new BinanceTradingAdapter(
      newClient(),
      retry(1),
      circuitBreaker(10, 10)
    );
  }

  private BrokerTradingPort adapter;

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
    return Retry.of("binance-test-retry-" + maxAttempts, config);
  }

  private CircuitBreaker circuitBreaker(int minCalls, int windowSize) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
      .minimumNumberOfCalls(minCalls)
      .slidingWindowSize(windowSize)
      .failureRateThreshold(50.0f)
      .waitDurationInOpenState(Duration.ofSeconds(30))
      .recordException(this::isRetryable)
      .build();
    return CircuitBreaker.of("binance-test-cb-" + minCalls + "-" + windowSize, config);
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
  void returnsOrderIdOn200() {
    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .willReturn(okJson("{\"orderId\":\"ord-123\"}")));

    BrokerOrderId orderId = adapter.placeBuyOrder(
      UUID.randomUUID(),
      InstrumentId.of("BTCUSDT"),
      Money.ars("15000.00")
    );

    assertEquals(BrokerOrderId.of("ord-123"), orderId);
  }

  @Test
  void throwsOn429() {
    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .willReturn(aResponse().withStatus(429).withBody("rate limited")));

    assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );
  }

  @Test
  void throwsOn500() {
    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .willReturn(serverError()));

    assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );
  }

  @Test
  void throwsOnTimeout() {
    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .willReturn(aResponse()
        .withStatus(200)
        .withFixedDelay(1000)
        .withBody("{\"orderId\":\"ord-123\"}")));

    assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );
  }

  @Test
  void throwsWhenOrderIdIsMissing() {
    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .willReturn(okJson("{\"status\":\"ok\"}")));

    assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );
  }

  @Test
  void retriesOnceOn500ThenSucceeds() {
    adapter = new BinanceTradingAdapter(
      newClient(),
      retry(2),
      circuitBreaker(10, 10)
    );

    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .inScenario("retry-order")
      .whenScenarioStateIs(STARTED)
      .willReturn(serverError())
      .willSetStateTo("SECOND_CALL"));

    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .inScenario("retry-order")
      .whenScenarioStateIs("SECOND_CALL")
      .willReturn(okJson("{\"orderId\":\"ord-999\"}")));

    BrokerOrderId orderId = adapter.placeBuyOrder(
      UUID.randomUUID(),
      InstrumentId.of("BTCUSDT"),
      Money.ars("15000.00")
    );

    assertEquals(BrokerOrderId.of("ord-999"), orderId);
    wm.verify(2, postRequestedFor(urlPathEqualTo("/api/v3/order")));
  }

  @Test
  void opensCircuitAfterConsecutiveFailures() {
    adapter = new BinanceTradingAdapter(
      newClient(),
      retry(1),
      circuitBreaker(2, 2)
    );

    wm.stubFor(post(urlPathEqualTo("/api/v3/order"))
      .willReturn(serverError()));

    assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );
    assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );

    IntegrationException ex = assertThrows(IntegrationException.class, () ->
      adapter.placeBuyOrder(UUID.randomUUID(), InstrumentId.of("BTCUSDT"), Money.ars("15000.00"))
    );

    assertInstanceOf(CallNotPermittedException.class, ex.getCause());
    wm.verify(2, postRequestedFor(urlPathEqualTo("/api/v3/order")));
  }

}
