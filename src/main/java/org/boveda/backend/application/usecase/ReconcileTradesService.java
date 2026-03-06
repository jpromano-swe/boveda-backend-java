package org.boveda.backend.application.usecase;

import org.boveda.backend.application.command.ReconcileTradesCommand;
import org.boveda.backend.application.dto.ReconcileTradesResult;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.model.Trade;
import org.boveda.backend.ports.in.ReconcileTradesUseCase;
import org.boveda.backend.ports.out.ExchangeOrderStatusPort;
import org.boveda.backend.ports.out.TradeRepository;

import java.util.List;
import java.util.Objects;

public class ReconcileTradesService implements ReconcileTradesUseCase {

  private final TradeRepository tradeRepository;
  private final ExchangeOrderStatusPort exchangeOrderStatusPort;

  public ReconcileTradesService(
    TradeRepository tradeRepository,
    ExchangeOrderStatusPort exchangeOrderStatusPort
  ) {
    this.tradeRepository = Objects.requireNonNull(tradeRepository);
    this.exchangeOrderStatusPort = Objects.requireNonNull(exchangeOrderStatusPort);
  }

  @Override
  public ReconcileTradesResult execute(ReconcileTradesCommand command) {
    Objects.requireNonNull(command, "command must not be null");

    if (command.batchSize() <= 0) {
      throw new ValidationException("batchSize must be positive");
    }
    if (command.now() == null) {
      throw new ValidationException("now must not be null");
    }

    List<Trade> pending = tradeRepository.findPendingTrades(command.batchSize());

    int reconciled = 0;
    int unchanged = 0;

    for (Trade trade : pending) {
      ExchangeOrderStatusPort.OrderStatus status =
        exchangeOrderStatusPort.fetchStatus(trade.brokerOrderId());

      Trade updated = switch (status) {
        case FILLED -> trade.markFilled(command.now());
        case CANCELED -> trade.markCanceled(command.now());
        case REJECTED -> trade.markRejected(command.now());
        case PENDING -> null;
      };

      if (updated == null) {
        unchanged++;
      } else {
        tradeRepository.save(updated);
        reconciled++;
      }
    }

    return new ReconcileTradesResult(reconciled, unchanged);
  }
}
