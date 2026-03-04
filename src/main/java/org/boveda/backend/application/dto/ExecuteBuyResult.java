package org.boveda.backend.application.dto;

import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.Money;

public record ExecuteBuyResult(
  BrokerOrderId brokerOrderId,
  Money feeAmount,
  Money netAmount,
  Money remainderAmount
) {}
