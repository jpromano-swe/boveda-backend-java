package org.boveda.backend.adapters.in.rest;

public record DepositItemResponse(
  String eventId,
  String source,
  String externalEventId,
  String amountArs,
  String occurredAt,
  String detectedAt,
  String correlationId
) {}
