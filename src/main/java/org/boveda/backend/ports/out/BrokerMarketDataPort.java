package org.boveda.backend.ports.out;

import org.boveda.backend.domain.vo.InstrumentId;

import java.math.BigDecimal;
import java.time.Instant;

public interface BrokerMarketDataPort {

  BrokerQuote fetchPrice(InstrumentId instrumentId);

  record BrokerQuote(
    InstrumentId instrumentId,
    BigDecimal price,
    Instant asOf,
    String source
  ) {}
}
