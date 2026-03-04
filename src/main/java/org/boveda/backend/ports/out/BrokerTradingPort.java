package org.boveda.backend.ports.out;

import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;

import java.util.UUID;

public interface BrokerTradingPort {
  BrokerOrderId placeBuyOrder(UUID userId, InstrumentId instrumentId, Money amount);
}
