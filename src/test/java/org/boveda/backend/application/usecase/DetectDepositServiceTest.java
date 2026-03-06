package org.boveda.backend.application.usecase;

import org.boveda.backend.application.command.DetectDepositCommand;
import org.boveda.backend.application.dto.DetectDepositResult;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.boveda.backend.ports.out.JobQueuePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectDepositServiceTest {

  @Mock
  private DepositEventRepository repository;

  @Mock
  private JobQueuePort jobQueuePort;

  private DetectDepositService service;

  @BeforeEach
  void setUp() {
    service = new DetectDepositService(repository, jobQueuePort);
  }

  @Test
  void createsDepositEventWhenExternalEventIsNew() {
    UUID userId = UUID.randomUUID();
    DetectDepositCommand command = new DetectDepositCommand(
      userId,
      "BINANCE",
      "dep-123",
      Money.ars("1000.00"),
      Instant.parse("2026-03-04T10:00:00Z"),
      " corr-001 "
    );

    when(repository.existsBySourceAndExternalEventId("BINANCE", "dep-123")).thenReturn(false);

    DetectDepositResult result = service.execute(command);

    assertEquals(DetectDepositResult.Status.CREATED, result.status());
    verify(repository).existsBySourceAndExternalEventId("BINANCE", "dep-123");

    ArgumentCaptor<DepositEvent> captor = ArgumentCaptor.forClass(DepositEvent.class);
    verify(repository).save(captor.capture());
    verify(jobQueuePort).enqueue(eq("DepositDetected"), eq("DEPOSIT"), any(UUID.class), contains("\"correlationId\":\"corr-001\""), any(Instant.class));

    DepositEvent saved = captor.getValue();
    assertNotNull(saved.eventId());
    assertEquals(userId, saved.userId());
    assertEquals("BINANCE", saved.source());
    assertEquals("dep-123", saved.externalEventId());
    assertEquals(Money.ars("1000.00"), saved.amount());
    assertEquals(Instant.parse("2026-03-04T10:00:00Z"), saved.occurredAt());
    assertEquals("corr-001", saved.correlationId());
    assertNotNull(saved.detectedAt());
    assertNotNull(saved.createdAt());
    assertNotNull(saved.updatedAt());
  }

  @Test
  void returnsDuplicateWhenExternalEventAlreadyExists() {
    DetectDepositCommand command = new DetectDepositCommand(
      UUID.randomUUID(),
      "BINANCE",
      "dep-123",
      Money.ars("1000.00"),
      Instant.parse("2026-03-04T10:00:00Z"),
      "corr-001"
    );

    when(repository.existsBySourceAndExternalEventId("BINANCE", "dep-123")).thenReturn(true);

    DetectDepositResult result = service.execute(command);

    assertEquals(DetectDepositResult.Status.DUPLICATE, result.status());
    verify(repository, never()).save(any());
    verify(jobQueuePort, never()).enqueue(any(), any(), any(), any(), any());

  }

  @Test
  void rejectsBlankSource() {
    DetectDepositCommand command = new DetectDepositCommand(
      UUID.randomUUID(),
      "   ",
      "dep-123",
      Money.ars("1000.00"),
      Instant.parse("2026-03-04T10:00:00Z"),
      "corr-001"
    );

    assertThrows(ValidationException.class, () -> service.execute(command));
    verify(repository, never()).existsBySourceAndExternalEventId(any(), any());
    verify(repository, never()).save(any());
    verifyNoInteractions(jobQueuePort);

  }

  @Test
  void rejectsBlankExternalEventId() {
    DetectDepositCommand command = new DetectDepositCommand(
      UUID.randomUUID(),
      "BINANCE",
      "   ",
      Money.ars("1000.00"),
      Instant.parse("2026-03-04T10:00:00Z"),
      "corr-001"
    );

    assertThrows(ValidationException.class, () -> service.execute(command));
    verify(repository, never()).existsBySourceAndExternalEventId(any(), any());
    verify(repository, never()).save(any());
    verifyNoInteractions(jobQueuePort);

  }

  @Test
  void rejectsNonPositiveAmount() {
    DetectDepositCommand command = new DetectDepositCommand(
      UUID.randomUUID(),
      "BINANCE",
      "dep-123",
      Money.ars("-1000.00"),
      Instant.parse("2026-03-04T10:00:00Z"),
      "corr-001"
    );

    assertThrows(ValidationException.class, () -> service.execute(command));
    verify(repository, never()).save(any());
    verifyNoInteractions(jobQueuePort);

  }

  @Test
  void rejectsNullCommand() {
    assertThrows(NullPointerException.class, () -> service.execute(null));
    verifyNoInteractions(repository);
    verifyNoInteractions(jobQueuePort);

  }
}
