package org.boveda.backend.application.dto;

public record ReconcileTradesResult(
  int reconciledCount,
  int unchangedCount
) {}
