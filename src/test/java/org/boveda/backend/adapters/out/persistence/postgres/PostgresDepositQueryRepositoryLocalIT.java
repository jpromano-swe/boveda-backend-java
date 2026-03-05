package org.boveda.backend.adapters.out.persistence.postgres;

import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("it-local")
class PostgresDepositQueryRepositoryLocalIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private DepositEventRepository eventRepository;

  @Autowired
  private DepositQueryRepository queryRepository;

  private UUID userId;

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("TRUNCATE TABLE deposit_events");
    userId = UUID.randomUUID();

    eventRepository.save(newEvent(userId, "BINANCE", "dep-001", "2026-03-05T10:00:00Z", "1000.00"));
    eventRepository.save(newEvent(userId, "BINANCE", "dep-002", "2026-03-05T11:00:00Z", "2000.00"));
    eventRepository.save(newEvent(userId, "IOL", "dep-003", "2026-03-05T12:00:00Z", "3000.00"));

    eventRepository.save(newEvent(UUID.randomUUID(), "BINANCE", "dep-999", "2026-03-05T11:30:00Z", "999.00"));
  }

  @Test
  void returnsOnlyUserDepositsOrderedByOccurredAtDesc() {
    List<DepositEvent> events = queryRepository.findByUserIdAndOccurredAtBetween(
      userId, null, null, 10
    );

    assertEquals(3, events.size());
    assertEquals("dep-003", events.get(0).externalEventId());
    assertEquals("dep-002", events.get(1).externalEventId());
    assertEquals("dep-001", events.get(2).externalEventId());
  }

  @Test
  void appliesDateRangeAndLimit() {
    Instant from = Instant.parse("2026-03-05T10:30:00Z");
    Instant to = Instant.parse("2026-03-05T12:30:00Z");

    List<DepositEvent> events = queryRepository.findByUserIdAndOccurredAtBetween(
      userId, from, to, 1
    );

    assertEquals(1, events.size());
    assertTrue(events.get(0).externalEventId().equals("dep-003") || events.get(0).externalEventId().equals("dep-002"));
    assertEquals("dep-003", events.get(0).externalEventId());
  }

  private DepositEvent newEvent(
    UUID userId,
    String source,
    String externalEventId,
    String occurredAt,
    String amountArs
  ) {
    Instant now = Instant.parse("2026-03-05T13:00:00Z");
    return new DepositEvent(
      UUID.randomUUID(),
      userId,
      source,
      externalEventId,
      Money.ars(amountArs),
      Instant.parse(occurredAt),
      now,
      "corr-" + externalEventId,
      now,
      now
    );
  }
}
