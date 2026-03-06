package org.boveda.backend.adapters.out.broker.binance;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.boveda.backend.domain.exception.IntegrationException;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.ports.out.BrokerMarketDataPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

@Component
public class BinanceMarketDataAdapter implements BrokerMarketDataPort {

  private final RestClient restClient;
  private final Retry retry;
  private final CircuitBreaker circuitBreaker;

  @Autowired
  public BinanceMarketDataAdapter(@Qualifier("binanceRestClient") RestClient restClient) {
    this(
      restClient,
      Retry.ofDefaults("binance-marketdata-retry"),
      CircuitBreaker.ofDefaults("binance-marketdata-cb")
    );
  }

  public BinanceMarketDataAdapter(RestClient restClient, Retry retry, CircuitBreaker circuitBreaker) {
    this.restClient = Objects.requireNonNull(restClient);
    this.retry = Objects.requireNonNull(retry);
    this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
  }

  @Override
  public BrokerQuote fetchPrice(InstrumentId instrumentId) {
    Objects.requireNonNull(instrumentId, "instrumentId must not be null");

    Supplier<BrokerQuote> resilientCall = Retry.decorateSupplier(
      retry,
      CircuitBreaker.decorateSupplier(circuitBreaker, () -> doFetchPrice(instrumentId))
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

  private BrokerQuote doFetchPrice(InstrumentId instrumentId) {
    BinanceTickerPriceResponse response = restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/api/v3/ticker/price")
        .queryParam("symbol", instrumentId.value())
        .build())
      .retrieve()
      .body(BinanceTickerPriceResponse.class);

    if (response == null || response.price() == null || response.price().isBlank()) {
      throw new IntegrationException("binance returned empty price response");
    }

    return new BrokerQuote(
      instrumentId,
      new BigDecimal(response.price()),
      Instant.now(),
      "BINANCE"
    );
  }

  private record BinanceTickerPriceResponse(String symbol, String price) {}
}
