package org.boveda.backend.ports.out;

import org.boveda.backend.domain.vo.BrokerOrderId;

public interface ExchangeOrderStatusPort {

  enum OrderStatus {
    PENDING,
    FILLED,
    CANCELED,
    REJECTED
  }

  OrderStatus fetchStatus(BrokerOrderId brokerOrderId);
}
