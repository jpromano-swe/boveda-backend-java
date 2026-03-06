package org.boveda.backend.ports.out;

import java.time.Instant;
import java.util.UUID;

public interface JobQueuePort {
  void enqueue(String eventType, String aggregateType, UUID aggregateId, String payload, Instant now);
}
