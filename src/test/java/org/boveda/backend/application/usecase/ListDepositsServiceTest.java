package org.boveda.backend.application.usecase;

import org.boveda.backend.application.dto.ListDepositsResult;
import org.boveda.backend.application.query.ListDepositsQuery;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    assertEquals("BINANCE", result.items().getFirst().source());
    assertEquals("dep-001", result.items().getFirst().externalEventId());
    assertEquals(Money.ars("1000.00"), result.items().getFirst().amount());
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
