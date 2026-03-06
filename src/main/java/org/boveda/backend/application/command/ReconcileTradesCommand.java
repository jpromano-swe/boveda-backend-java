package org.boveda.backend.application.command;

import java.time.Instant;

public record ReconcileTradesCommand(
  int batchSize,
  Instant now
) {}
