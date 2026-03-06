package org.boveda.backend.adapters.out.broker.binance;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.boveda.backend.domain.exception.IntegrationException;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.BrokerTradingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class BinanceTradingAdapter implements BrokerTradingPort {

  private final RestClient restClient;
  private final Retry retry;
  private final CircuitBreaker circuitBreaker;

  @Autowired
  public BinanceTradingAdapter(
    @Qualifier("binanceRestClient") RestClient restClient,
    @Qualifier("binanceTradingRetry") Retry retry,
    @Qualifier("binanceTradingCircuitBreaker") CircuitBreaker circuitBreaker
  ) {
    this.restClient = Objects.requireNonNull(restClient);
    this.retry = Objects.requireNonNull(retry);
    this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
  }

  @Override
  public BrokerOrderId placeBuyOrder(UUID userId, InstrumentId instrumentId, Money amount) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(instrumentId, "instrumentId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");

    Supplier<BrokerOrderId> resilientCall = Retry.decorateSupplier(
      retry,
      CircuitBreaker.decorateSupplier(circuitBreaker, () -> doPlaceBuyOrder(instrumentId, amount))
    );

    try {
      return resilientCall.get();
    } catch (CallNotPermittedException ex) {
      throw new IntegrationException("binance circuit breaker is open", ex);
    } catch (RestClientResponseException ex) {
      throw new IntegrationException("binance http error: " + ex.getStatusCode(), ex);
    } catch (ResourceAccessException ex) {
      throw new IntegrationException("binance timeout/access error", ex);
    } catch (IntegrationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IntegrationException("binance mapping error", ex);
    }
  }

  private BrokerOrderId doPlaceBuyOrder(InstrumentId instrumentId, Money amount) {
    BinanceOrderResponse response = restClient.post()
      .uri("/api/v3/order")
      .body(Map.of(
        "symbol", instrumentId.value(),
        "side", "BUY",
        "quoteOrderQty", amount.amount().toPlainString()
      ))
      .retrieve()
      .body(BinanceOrderResponse.class);

    if (response == null || response.orderId() == null || response.orderId().isBlank()) {
      throw new IntegrationException("binance returned empty orderId");
    }

    return BrokerOrderId.of(response.orderId());
  }

  private record BinanceOrderResponse(String orderId) {}
}
