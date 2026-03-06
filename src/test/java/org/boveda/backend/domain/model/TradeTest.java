package org.boveda.backend.domain.model;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TradeTest {

  @Test
  void marksPendingTradeAsFilled(){
    Trade trade = pendingTrade();

    Instant now = Instant.parse("2026-03-05T12:00:00Z");
    Trade updated = trade.markFilled(now);

    assertEquals(TradeStatus.FILLED, updated.status());
    assertEquals(now, updated.updatedAt());
    assertEquals(now, updated.executedAt());
  }

  @Test
  void rejectsTransitionFromFinalState(){
    Trade filled = pendingTrade().markFilled(Instant.parse("2026-03-05T12:05:00Z"));

    assertThrows(BusinessRuleViolationException.class,
      ()-> filled.markCanceled(Instant.parse("2026-03-06T12:05:00Z")));
  }

  private Trade pendingTrade() {
    Instant created = Instant.parse("2026-03-06T10:00:00Z");
    return new Trade(
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      InstrumentId.of("BTCUSDT"),
      BrokerOrderId.of("ORD-001"),
      Money.ars("1000.00"),
      Money.ars("25.00"),
      Money.ars("975.00"),
      TradeStatus.PENDING,
      created,
      created,
      null
    );
  }
}
