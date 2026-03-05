package org.boveda.backend.adapters.out.persistence.postgres;


import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class PostgresDepositQueryRepository implements DepositQueryRepository {

  private final JdbcTemplate jdbcTemplate;

  public PostgresDepositQueryRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
  }

  @Override
  public List<DepositEvent> findByUserIdAndOccurredAtBetween(UUID userId, Instant from, Instant to, int limit) {
    StringBuilder sql = new StringBuilder("""
      select event_id, user_id, source, external_event_id, amount, currency, occurred_at, detected_at, correlation_id, created_at, updated_at
      from deposit_events
      where user_id = ?
      """);

    List<Object> params = new ArrayList<>();
    params.add(userId);

    if (from != null) {
      sql.append(" and occurred_at >= ?");
      params.add(Timestamp.from(from));
    }

    if (to != null) {
      sql.append(" and occurred_at <= ?");
      params.add(Timestamp.from(to));
    }

    sql.append(" order by occurred_at desc limit ?");
    params.add(limit);

    return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new DepositEvent(
      rs.getObject("event_id", UUID.class),
      rs.getObject("user_id", UUID.class),
      rs.getString("source"),
      rs.getString("external_event_id"),
      new Money(rs.getBigDecimal("amount"), Currency.getInstance(rs.getString("currency"))),
      rs.getTimestamp("occurred_at").toInstant(),
      rs.getTimestamp("detected_at").toInstant(),
      rs.getString("correlation_id"),
      rs.getTimestamp("created_at").toInstant(),
      rs.getTimestamp("updated_at").toInstant()
    ), params.toArray());
  }
}
