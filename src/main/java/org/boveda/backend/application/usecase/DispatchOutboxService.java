package org.boveda.backend.application.usecase;

import org.boveda.backend.domain.model.OutboxEvent;
import org.boveda.backend.ports.out.OutboxRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class DispatchOutboxService {

  private final OutboxRepository outboxRepository;
  private final OutboxEventDispatcher dispatcher;
  private final int batchSize;
  private final int maxAttempts;
  private final int retryDelaySeconds;

  public DispatchOutboxService(
    OutboxRepository outboxRepository,
    OutboxEventDispatcher dispatcher,
    int batchSize,
    int maxAttempts,
    int retryDelaySeconds
  ) {
    this.outboxRepository = Objects.requireNonNull(outboxRepository);
    this.dispatcher = Objects.requireNonNull(dispatcher);

    if (batchSize <= 0) throw new IllegalArgumentException("batchSize must be positive");
    if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be positive");
    if (retryDelaySeconds <= 0) throw new IllegalArgumentException("retryDelaySeconds must be positive");

    this.batchSize = batchSize;
    this.maxAttempts = maxAttempts;
    this.retryDelaySeconds = retryDelaySeconds;
  }

  public DispatchOutboxResult dispatch(Instant now) {
    Objects.requireNonNull(now, "now must not be null");

    List<OutboxEvent> events = outboxRepository.findDispatchable(now, batchSize);

    int sent = 0;
    int retry = 0;
    int dead = 0;
    int skipped = 0;

    for (OutboxEvent event : events) {
      boolean locked = outboxRepository.markProcessing(event.id(), now);
      if (!locked) {
        skipped++;
        continue;
      }

      try {
        dispatcher.dispatch(event);
        outboxRepository.markSent(event.id(), now);
        sent++;
      } catch (Exception ex) {
        String error = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();

        if (event.attempts() >= maxAttempts) {
          outboxRepository.markDead(event.id(), error, now);
          dead++;
        } else {
          outboxRepository.markRetry(
            event.id(),
            now.plusSeconds(retryDelaySeconds),
            error,
            now
          );
          retry++;
        }
      }
    }

    return new DispatchOutboxResult(sent, retry, dead, skipped);
  }
}
