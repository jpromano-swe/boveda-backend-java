package org.boveda.backend.ports.out;

import org.boveda.backend.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {
  OutboxEvent save(OutboxEvent event);

  List<OutboxEvent> findDispatchable(Instant now, int batchSize);

  boolean markSent(UUID id, Instant now);

  boolean markRetry(UUID id, Instant nextRetryAt, String lastError, Instant now);

  boolean markProcessing(UUID id, Instant now);

  boolean markDead(UUID id, String LastError, Instant now);
}
