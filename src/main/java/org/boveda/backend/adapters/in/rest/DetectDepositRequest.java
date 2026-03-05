package org.boveda.backend.adapters.in.rest;

import jakarta.validation.constraints.NotBlank;

public record DetectDepositRequest(
  @NotBlank String userId,
  @NotBlank String source,
  @NotBlank String externalEventId,
  @NotBlank String amountArs,
  @NotBlank String occurredAt,
  String correlationId
) {}
