package org.boveda.backend.application.dto;

import org.boveda.backend.domain.vo.Money;

import java.time.Instant;
import java.util.UUID;

public record DepositListItem(
  UUID eventId,
  String source,
  String externalEventId,
  Money amount,
  Instant occurredAt,
  Instant detectedAt,
  String correlationId
) {
}
