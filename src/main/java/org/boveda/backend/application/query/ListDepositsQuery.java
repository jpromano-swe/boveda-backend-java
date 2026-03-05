package org.boveda.backend.application.query;

import java.time.Instant;
import java.util.UUID;

public record ListDepositsQuery(
  UUID userId,
  Instant from,
  Instant to,
  int limit
) {}
