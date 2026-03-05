package org.boveda.backend.adapters.out.persistence.postgres;

import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;

import java.util.Objects;

@Repository
public class PostgresDepositEventRepository implements DepositEventRepository {

  private final JdbcTemplate jdbcTemplate;

  public PostgresDepositEventRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
  }

  @Override
  public boolean existsBySourceAndExternalEventId(String source, String externalEventId) {
    Boolean exists = jdbcTemplate.queryForObject(
      """
      select exists(
        select 1
        from deposit_events
        where source = ? and external_event_id = ?
      )
      """,
      Boolean.class,
      source,
      externalEventId
    );
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public DepositEvent save(DepositEvent event) {
    jdbcTemplate.update(
      """
      insert into deposit_events (
        event_id,
        user_id,
        source,
        external_event_id,
        amount,
        currency,
        occurred_at,
        detected_at,
        correlation_id,
        created_at,
        updated_at
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """,
      event.eventId(),
      event.userId(),
      event.source(),
      event.externalEventId(),
      event.amount().amount(),
      event.amount().currency().getCurrencyCode(),
      Timestamp.from(event.occurredAt()),
      Timestamp.from(event.detectedAt()),
      event.correlationId(),
      Timestamp.from(event.createdAt()),
      Timestamp.from(event.updatedAt())
    );
    return event;
  }
}
