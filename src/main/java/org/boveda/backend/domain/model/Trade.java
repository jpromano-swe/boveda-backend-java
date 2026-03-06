package org.boveda.backend.domain.model;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Trade(
  UUID tradeId,
  UUID userId,
  UUID strategyId,
  InstrumentId instrumentId,
  BrokerOrderId brokerOrderId,
  Money grossAmount,
  Money feeAmount,
  Money netAmount,
  TradeStatus status,
  Instant createdAt,
  Instant updatedAt,
  Instant executedAt
) {

  public Trade {
    Objects.requireNonNull(tradeId, "tradeId must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(strategyId, "strategyId must not be null");
    Objects.requireNonNull(instrumentId, "instrumentId must not be null");
    Objects.requireNonNull(brokerOrderId, "brokerOrderId must not be null");
    Objects.requireNonNull(grossAmount, "grossAmount must not be null");
    Objects.requireNonNull(feeAmount, "feeAmount must not be null");
    Objects.requireNonNull(netAmount, "netAmount must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
  }

  public Trade markFilled(Instant now){
    Objects.requireNonNull(now, "now must not be null");
    ensureNotFinal();
    return new Trade(
      tradeId,
      userId,
      strategyId,
      instrumentId,
      brokerOrderId,
      grossAmount,
      feeAmount,
      netAmount,
      TradeStatus.FILLED,
      createdAt,
      now,
      now
    );
  }

  public Trade markCanceled(Instant now) {
    Objects.requireNonNull(now, "now must not be null");
    ensureNotFinal();
    return new Trade(
      tradeId,
      userId,
      strategyId,
      instrumentId,
      brokerOrderId,
      grossAmount,
      feeAmount,
      netAmount,
      TradeStatus.CANCELED,
      createdAt,
      now,
      executedAt
    );
  }

  public Trade markRejected(Instant now) {
    Objects.requireNonNull(now, "now must not be null");
    ensureNotFinal();
    return new Trade(
      tradeId,
      userId,
      strategyId,
      instrumentId,
      brokerOrderId,
      grossAmount,
      feeAmount,
      netAmount,
      TradeStatus.REJECTED,
      createdAt,
      now,
      executedAt
    );
  }

  private void ensureNotFinal() {
    if (status.isFinal()) {
      throw new BusinessRuleViolationException("cannot transition from final trade status");
    }
  }
}
