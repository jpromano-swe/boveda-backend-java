package org.boveda.backend.ports.out;

import org.boveda.backend.domain.model.Trade;

import java.util.List;

public interface TradeRepository {
  List<Trade> findPendingTrades(int limit);
  Trade save(Trade trade);
}
