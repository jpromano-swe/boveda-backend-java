package org.boveda.backend.domain.model;

import org.boveda.backend.domain.vo.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DepositEvent(
  UUID eventId,
  UUID userId,
  String source,
  String externalEventId,
  Money amount,
  Instant occurredAt,
  Instant detectedAt,
  String correlationId,
  Instant createdAt,
  Instant updatedAt
) {
  public DepositEvent {
      Objects.requireNonNull(eventId);
      Objects.requireNonNull(userId);
      Objects.requireNonNull(source);
      Objects.requireNonNull(externalEventId);
      Objects.requireNonNull(amount);
      Objects.requireNonNull(occurredAt);
      Objects.requireNonNull(detectedAt);
      Objects.requireNonNull(correlationId);
      Objects.requireNonNull(createdAt);
      Objects.requireNonNull(updatedAt);
    }
}
