package org.boveda.backend.application.command;

import java.util.UUID;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.domain.vo.Percentage;

public record ExecuteBuyCommand(
  UUID userId,
  UUID strategyId,
  InstrumentId instrumentId,
  Money grossAmount,
  Money minimumOrderAmount,
  Percentage feeRate,
  String idempotencyKey
) {}
