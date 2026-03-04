package org.boveda.backend.application.command;

import org.boveda.backend.domain.vo.Money;

import java.time.Instant;
import java.util.UUID;

public record DetectDepositCommand(
  UUID userId,
  String source,
  String externalEventId,
  Money amount,
  Instant occurredAt,
  String correlationId
) {}
