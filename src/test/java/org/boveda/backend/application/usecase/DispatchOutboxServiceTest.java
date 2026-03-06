package org.boveda.backend.application.usecase;

import org.boveda.backend.domain.model.OutboxEvent;
import org.boveda.backend.domain.model.OutboxStatus;
import org.boveda.backend.ports.out.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchOutboxServiceTest {

  @Mock
  private OutboxRepository outboxRepository;

  @Mock
  private OutboxEventDispatcher dispatcher;

  private DispatchOutboxService service;

  @BeforeEach
  void setUp() {
    service = new DispatchOutboxService(outboxRepository, dispatcher, 5, 3, 30);
  }

  @Test
  void dispatchesAndMarksSent() {
    Instant now = Instant.parse("2026-03-06T14:00:00Z");
    OutboxEvent event = pendingEvent(now);

    when(outboxRepository.findDispatchable(now, 5)).thenReturn(List.of(event));
    when(outboxRepository.markProcessing(event.id(), now)).thenReturn(true);

    DispatchOutboxResult result = service.dispatch(now);

    assertEquals(1, result.sentCount());
    assertEquals(0, result.retryCount());
    assertEquals(0, result.deadCount());
    assertEquals(0, result.skippedCount());

    verify(dispatcher).dispatch(event);
    verify(outboxRepository).markSent(event.id(), now);
  }

  @Test
  void marksRetryOnDispatcherError() {
    Instant now = Instant.parse("2026-03-06T14:00:00Z");
    OutboxEvent event = pendingEvent(now);

    when(outboxRepository.findDispatchable(now, 5)).thenReturn(List.of(event));
    when(outboxRepository.markProcessing(event.id(), now)).thenReturn(true);
    doThrow(new RuntimeException("temporary")).when(dispatcher).dispatch(event);

    DispatchOutboxResult result = service.dispatch(now);

    assertEquals(0, result.sentCount());
    assertEquals(1, result.retryCount());
    assertEquals(0, result.deadCount());

    verify(outboxRepository).markRetry(eq(event.id()), any(Instant.class), contains("temporary"), eq(now));
  }

  @Test
  void marksDeadWhenAttemptsReachedLimit() {
    Instant now = Instant.parse("2026-03-06T14:00:00Z");
    OutboxEvent event = retryEventWithAttempts(now, 3);

    when(outboxRepository.findDispatchable(now, 5)).thenReturn(List.of(event));
    when(outboxRepository.markProcessing(event.id(), now)).thenReturn(true);
    doThrow(new RuntimeException("still failing")).when(dispatcher).dispatch(event);

    DispatchOutboxResult result = service.dispatch(now);

    assertEquals(0, result.sentCount());
    assertEquals(0, result.retryCount());
    assertEquals(1, result.deadCount());

    verify(outboxRepository).markDead(event.id(), "still failing", now);
    verify(outboxRepository, never()).markRetry(any(), any(), any(), any());
  }

  @Test
  void skipsEventWhenProcessingLockFails() {
    Instant now = Instant.parse("2026-03-06T14:00:00Z");
    OutboxEvent event = pendingEvent(now);

    when(outboxRepository.findDispatchable(now, 5)).thenReturn(List.of(event));
    when(outboxRepository.markProcessing(event.id(), now)).thenReturn(false);

    DispatchOutboxResult result = service.dispatch(now);

    assertEquals(1, result.skippedCount());
    verifyNoInteractions(dispatcher);
    verify(outboxRepository, never()).markSent(any(), any());
  }

  private OutboxEvent pendingEvent(Instant now) {
    return new OutboxEvent(
      UUID.randomUUID(),
      "DepositDetected",
      "DEPOSIT",
      UUID.randomUUID(),
      "{\"ok\":true}",
      OutboxStatus.PENDING,
      0,
      now,
      null,
      now,
      now
    );
  }

  private OutboxEvent retryEventWithAttempts(Instant now, int attempts) {
    return new OutboxEvent(
      UUID.randomUUID(),
      "DepositDetected",
      "DEPOSIT",
      UUID.randomUUID(),
      "{\"ok\":true}",
      OutboxStatus.RETRY,
      attempts,
      now,
      "prev error",
      now.minusSeconds(120),
      now.minusSeconds(60)
    );
  }
}
