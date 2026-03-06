package org.boveda.backend.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OutboxEvent(
  UUID id,
  String eventType,
  String aggregateType,
  UUID aggregateId,
  String payload,
  OutboxStatus status,
  int attempts,
  Instant nextRetryAt,
  String lastError,
  Instant createdAt,
  Instant updatedAt
) {
  public OutboxEvent {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(eventType, "eventType must not be null");
    Objects.requireNonNull(aggregateType, "aggregateType must not be null");
    Objects.requireNonNull(aggregateId, "aggregateId must not be null");
    Objects.requireNonNull(payload, "payload must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(nextRetryAt, "nextRetryAt must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    if (attempts < 0) {
      throw new IllegalArgumentException("attempts must be >= 0");
    }
  }
}
