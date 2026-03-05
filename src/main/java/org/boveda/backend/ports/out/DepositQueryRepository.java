package org.boveda.backend.ports.out;

import org.boveda.backend.domain.model.DepositEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DepositQueryRepository {
  List<DepositEvent> findByUserIdAndOccurredAtBetween(UUID userId, Instant from, Instant to, int limit);
}
