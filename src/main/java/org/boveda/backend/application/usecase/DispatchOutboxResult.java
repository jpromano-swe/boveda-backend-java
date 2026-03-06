package org.boveda.backend.application.usecase;

public record DispatchOutboxResult(
  int sentCount,
  int retryCount,
  int deadCount,
  int skippedCount
) {}
