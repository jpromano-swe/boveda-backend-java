package org.boveda.backend.adapters.out.persistence.postgres;

import org.boveda.backend.domain.model.OutboxEvent;
import org.boveda.backend.domain.model.OutboxStatus;
import org.boveda.backend.ports.out.JobQueuePort;
import org.boveda.backend.ports.out.OutboxRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
public class OutboxJobQueueAdapter implements JobQueuePort {

  private final OutboxRepository outboxRepository;

  public OutboxJobQueueAdapter(OutboxRepository outboxRepository) {
    this.outboxRepository = Objects.requireNonNull(outboxRepository);
  }

  @Override
  public void enqueue(String eventType, String aggregateType, UUID aggregateId, String payload, Instant now) {
    OutboxEvent event = new OutboxEvent(
      UUID.randomUUID(),
      eventType,
      aggregateType,
      aggregateId,
      payload,
      OutboxStatus.PENDING,
      0,
      now,
      null,
      now,
      now
    );
    outboxRepository.save(event);
  }
}
