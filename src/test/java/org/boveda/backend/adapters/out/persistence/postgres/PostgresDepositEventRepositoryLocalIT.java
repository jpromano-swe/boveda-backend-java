package org.boveda.backend.adapters.out.persistence.postgres;

import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("it-local")
class PostgresDepositEventRepositoryLocalIT {

  @Autowired
  private DepositEventRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clean() {
    jdbcTemplate.execute("TRUNCATE TABLE deposit_events");
  }

  @Test
  void saveThenExistsReturnsTrue() {
    DepositEvent event = newEvent("BINANCE", "dep-001");
    repository.save(event);

    assertTrue(repository.existsBySourceAndExternalEventId("BINANCE", "dep-001"));
  }

  @Test
  void existsReturnsFalseWhenMissing() {
    assertFalse(repository.existsBySourceAndExternalEventId("BINANCE", "missing"));
  }

  private DepositEvent newEvent(String source, String externalEventId) {
    Instant now = Instant.parse("2026-03-05T12:00:00Z");
    return new DepositEvent(
      UUID.randomUUID(),
      UUID.randomUUID(),
      source,
      externalEventId,
      Money.ars("1000.00"),
      now,
      now,
      "corr-001",
      now,
      now
    );
  }
}
