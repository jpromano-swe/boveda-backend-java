package org.boveda.backend.application.usecase;

import org.boveda.backend.application.command.ReconcileTradesCommand;
import org.boveda.backend.application.dto.ReconcileTradesResult;
import org.boveda.backend.domain.model.Trade;
import org.boveda.backend.domain.model.TradeStatus;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.ExchangeOrderStatusPort;
import org.boveda.backend.ports.out.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReconcileTradesServiceTest {

  @Mock
  private TradeRepository tradeRepository;

  @Mock
  private ExchangeOrderStatusPort exchangeOrderStatusPort;

  private ReconcileTradesService service;

  @BeforeEach
  void setUp(){
    service = new ReconcileTradesService(tradeRepository, exchangeOrderStatusPort);
  }

  @Test
  void marksPendingTradeAsFilledWhenBrokerSaysFilled(){
    Trade pending = pendingTrade("ORD-001");
    when(tradeRepository.findPendingTrades(100)).thenReturn(List.of(pending));
    when(exchangeOrderStatusPort.fetchStatus(BrokerOrderId.of("ORD-001")))
      .thenReturn(ExchangeOrderStatusPort.OrderStatus.FILLED);

    ReconcileTradesResult result = service.execute(new ReconcileTradesCommand(100, Instant.parse("2026-03-06T12:00:00Z")));

    assertEquals(1, result.reconciledCount());
    assertEquals(0, result.unchangedCount());

    verify(tradeRepository).save(argThat(t -> t.status() == TradeStatus.FILLED));
  }

  @Test
  void keepsPendingTradeWhenBrokerSaysPending() {
    Trade pending = pendingTrade("ORD-002");
    when(tradeRepository.findPendingTrades(100)).thenReturn(List.of(pending));
    when(exchangeOrderStatusPort.fetchStatus(BrokerOrderId.of("ORD-002")))
      .thenReturn(ExchangeOrderStatusPort.OrderStatus.PENDING);

    ReconcileTradesResult result = service.execute(new ReconcileTradesCommand(100, Instant.parse("2026-03-06T12:00:00Z")));

    assertEquals(0, result.reconciledCount());
    assertEquals(1, result.unchangedCount());

    verify(tradeRepository, never()).save(any());
  }

  @Test
  void marksPendingTradeAsCanceledWhenBrokerSaysCanceled() {
    Trade pending = pendingTrade("ORD-003");
    when(tradeRepository.findPendingTrades(100)).thenReturn(List.of(pending));
    when(exchangeOrderStatusPort.fetchStatus(BrokerOrderId.of("ORD-003")))
      .thenReturn(ExchangeOrderStatusPort.OrderStatus.CANCELED);

    ReconcileTradesResult result = service.execute(new ReconcileTradesCommand(100, Instant.parse("2026-03-06T12:00:00Z")));

    assertEquals(1, result.reconciledCount());
    assertEquals(0, result.unchangedCount());

    verify(tradeRepository).save(argThat(t -> t.status() == TradeStatus.CANCELED));
  }

  private Trade pendingTrade(String brokerOrderId) {
    Instant created = Instant.parse("2026-03-06T10:00:00Z");
    return new Trade(
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      InstrumentId.of("BTCUSDT"),
      BrokerOrderId.of(brokerOrderId),
      Money.ars("1000.00"),
      Money.ars("25.00"),
      Money.ars("975.00"),
      TradeStatus.PENDING,
      created,
      created,
      null
    );
  }

  @Test
  void rejectsNonPositiveBatchSize() {
    ReconcileTradesCommand command = new ReconcileTradesCommand(0, Instant.parse("2026-03-06T12:00:00Z"));

    org.junit.jupiter.api.Assertions.assertThrows(
      org.boveda.backend.domain.exception.ValidationException.class,
      () -> service.execute(command)
    );

    verifyNoInteractions(tradeRepository, exchangeOrderStatusPort);
  }

  @Test
  void rejectsNullNow() {
    ReconcileTradesCommand command = new ReconcileTradesCommand(100, null);

    org.junit.jupiter.api.Assertions.assertThrows(
      org.boveda.backend.domain.exception.ValidationException.class,
      () -> service.execute(command)
    );

    verifyNoInteractions(tradeRepository, exchangeOrderStatusPort);
  }
}
