package org.boveda.backend.application.usecase;

import org.boveda.backend.application.command.DetectDepositCommand;
import org.boveda.backend.application.dto.DetectDepositResult;
import org.boveda.backend.application.dto.ListDepositsResult;
import org.boveda.backend.application.query.ListDepositsQuery;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectDepositServiceTest {

  @Mock
  private DepositEventRepository repository;

  private DetectDepositService service;

  @BeforeEach
  void setUp() {
    service = new DetectDepositService(repository);
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
  }

  @Test
  void rejectsNullCommand() {
    assertThrows(NullPointerException.class, () -> service.execute(null));
    verifyNoInteractions(repository);
  }

    @ExtendWith(MockitoExtension.class)
    static
    class ListDepositsServiceTest {

      @Mock
      private DepositQueryRepository repository;

      private ListDepositsService service;

      @BeforeEach
      void setUp() {
        service = new ListDepositsService(repository);
      }

      @Test
      void returnsMappedItems() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-05T12:00:00Z");

        DepositEvent event = new DepositEvent(
          UUID.randomUUID(),
          userId,
          "BINANCE",
          "dep-001",
          Money.ars("1000.00"),
          now,
          now,
          "corr-001",
          now,
          now
        );

        when(repository.findByUserIdAndOccurredAtBetween(userId, null, null, 50))
          .thenReturn(List.of(event));

        ListDepositsResult result = service.execute(new ListDepositsQuery(userId, null, null, 50));

        assertEquals(1, result.items().size());
        assertEquals("BINANCE", result.items().get(0).source());
        assertEquals("dep-001", result.items().get(0).externalEventId());
        assertEquals(Money.ars("1000.00"), result.items().get(0).amount());
      }

      @Test
      void rejectsInvalidLimit() {
        UUID userId = UUID.randomUUID();
        assertThrows(ValidationException.class, () -> service.execute(new ListDepositsQuery(userId, null, null, 0)));
        assertThrows(ValidationException.class, () -> service.execute(new ListDepositsQuery(userId, null, null, 501)));
      }

      @Test
      void rejectsInvalidRange() {
        UUID userId = UUID.randomUUID();
        Instant from = Instant.parse("2026-03-06T00:00:00Z");
        Instant to = Instant.parse("2026-03-05T00:00:00Z");

        assertThrows(ValidationException.class, () -> service.execute(new ListDepositsQuery(userId, from, to, 50)));
      }
    }
}
