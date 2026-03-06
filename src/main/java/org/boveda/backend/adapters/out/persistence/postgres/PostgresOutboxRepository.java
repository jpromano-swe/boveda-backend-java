package org.boveda.backend.adapters.out.persistence.postgres;

import org.boveda.backend.domain.model.OutboxEvent;
import org.boveda.backend.domain.model.OutboxStatus;
import org.boveda.backend.ports.out.OutboxRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class PostgresOutboxRepository implements OutboxRepository {

  private final JdbcTemplate jdbcTemplate;

  public PostgresOutboxRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
  }

  @Override
  public OutboxEvent save(OutboxEvent event) {
    jdbcTemplate.update(
      """
      insert into outbox (
        id,
        event_type,
        aggregate_type,
        aggregate_id,
        payload,
        status,
        attempts,
        next_retry_at,
        last_error,
        created_at,
        updated_at
      ) values (?, ?, ?, ?, cast(? as jsonb), ?, ?, ?, ?, ?, ?)
      """,
      event.id(),
      event.eventType(),
      event.aggregateType(),
      event.aggregateId(),
      event.payload(),
      event.status().name(),
      event.attempts(),
      Timestamp.from(event.nextRetryAt()),
      event.lastError(),
      Timestamp.from(event.createdAt()),
      Timestamp.from(event.updatedAt())
    );
    return event;
  }

  @Override
  public List<OutboxEvent> findDispatchable(Instant now, int batchSize) {
    return jdbcTemplate.query(
      """
      select id, event_type, aggregate_type, aggregate_id, payload, status, attempts, next_retry_at, last_error, created_at, updated_at
      from outbox
      where status in ('PENDING', 'RETRY')
        and next_retry_at <= ?
      order by created_at asc
      limit ?
      """,
      (rs, rowNum) -> new OutboxEvent(
        rs.getObject("id", UUID.class),
        rs.getString("event_type"),
        rs.getString("aggregate_type"),
        rs.getObject("aggregate_id", UUID.class),
        rs.getString("payload"),
        OutboxStatus.valueOf(rs.getString("status")),
        rs.getInt("attempts"),
        rs.getTimestamp("next_retry_at").toInstant(),
        rs.getString("last_error"),
        rs.getTimestamp("created_at").toInstant(),
        rs.getTimestamp("updated_at").toInstant()
      ),
      Timestamp.from(now),
      batchSize
    );
  }

  @Override
  public boolean markSent(UUID id, Instant now) {
    int updated = jdbcTemplate.update(
      """
      update outbox
      set status = 'SENT',
          updated_at = ?
      where id = ?
      """,
      Timestamp.from(now),
      id
    );
    return updated == 1;
  }

  @Override
  public boolean markRetry(UUID id, Instant nextRetryAt, String lastError, Instant now) {
    int updated = jdbcTemplate.update(
      """
      update outbox
      set status = 'RETRY',
          attempts = attempts + 1,
          next_retry_at = ?,
          last_error = ?,
          updated_at = ?
      where id = ?
      """,
      Timestamp.from(nextRetryAt),
      lastError,
      Timestamp.from(now),
      id
    );
    return updated == 1;
  }

  @Override
  public boolean markProcessing(UUID id, Instant now) {
    int updated = jdbcTemplate.update(
      """
      update outbox
      set status = 'PROCESSING',
          updated_at = ?
      where id = ?
        and status in ('PENDING', 'RETRY')
      """,
      Timestamp.from(now),
      id
    );
    return updated == 1;
  }

  @Override
  public boolean markDead(UUID id, String lastError, Instant now) {
    int updated = jdbcTemplate.update(
      """
      update outbox
      set status = 'DEAD',
          attempts = attempts + 1,
          last_error = ?,
          updated_at = ?
      where id = ?
      """,
      lastError,
      Timestamp.from(now),
      id
    );
    return updated == 1;
  }

}
