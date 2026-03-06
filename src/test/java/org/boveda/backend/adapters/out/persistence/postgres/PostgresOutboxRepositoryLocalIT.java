package org.boveda.backend.adapters.out.persistence.postgres;

import org.boveda.backend.domain.model.OutboxEvent;
import org.boveda.backend.domain.model.OutboxStatus;
import org.boveda.backend.ports.out.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("it-local")
class PostgresOutboxRepositoryLocalIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private OutboxRepository outboxRepository;

  @BeforeEach
  void clean() {
    jdbcTemplate.execute("TRUNCATE TABLE outbox");
  }

  @Test
  void savesAndFindsDispatchableEventsOrdered() {
    Instant base = Instant.parse("2026-03-06T12:00:00Z");

    OutboxEvent older = newEvent("DepositDetected", base.minusSeconds(60));
    OutboxEvent newer = newEvent("DepositDetected", base.minusSeconds(30));

    outboxRepository.save(newer);
    outboxRepository.save(older);

    List<OutboxEvent> events = outboxRepository.findDispatchable(base, 10);

    assertEquals(2, events.size());
    assertEquals(older.id(), events.get(0).id());
    assertEquals(newer.id(), events.get(1).id());
  }

  @Test
  void marksSent() {
    Instant now = Instant.parse("2026-03-06T12:00:00Z");
    OutboxEvent event = outboxRepository.save(newEvent("DepositDetected", now));

    boolean updated = outboxRepository.markSent(event.id(), now.plusSeconds(5));
    assertTrue(updated);

    String status = jdbcTemplate.queryForObject(
      "select status from outbox where id = ?",
      String.class,
      event.id()
    );

    assertEquals("SENT", status);
  }

  @Test
  void marksRetryAndIncrementsAttempts() {
    Instant now = Instant.parse("2026-03-06T12:00:00Z");
    OutboxEvent event = outboxRepository.save(newEvent("DepositDetected", now));

    Instant nextRetry = now.plusSeconds(60);
    boolean updated = outboxRepository.markRetry(event.id(), nextRetry, "temporary error", now.plusSeconds(5));
    assertTrue(updated);

    Integer attempts = jdbcTemplate.queryForObject(
      "select attempts from outbox where id = ?",
      Integer.class,
      event.id()
    );
    String status = jdbcTemplate.queryForObject(
      "select status from outbox where id = ?",
      String.class,
      event.id()
    );

    assertEquals(1, attempts);
    assertEquals("RETRY", status);
  }

  private OutboxEvent newEvent(String eventType, Instant now) {
    return new OutboxEvent(
      UUID.randomUUID(),
      eventType,
      "DEPOSIT",
      UUID.randomUUID(),
      "{\"sample\":true}",
      OutboxStatus.PENDING,
      0,
      now,
      null,
      now,
      now
    );
  }
}
