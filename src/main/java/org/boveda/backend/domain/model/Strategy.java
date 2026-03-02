package org.boveda.backend.domain.model;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record Strategy(
  UUID strategyId,
  UUID userId,
  List<Allocation> allocations,
  CashReserve cashReserve
) {

  private static final BigDecimal HUNDRED = new BigDecimal("100.00");

  public Strategy {
    Objects.requireNonNull(strategyId, "strategyId must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(allocations, "allocations must not be null");
    Objects.requireNonNull(cashReserve, "cashReserve must not be null");

    if (allocations.isEmpty()) {
      throw new BusinessRuleViolationException("Strategy must contain at least one allocation");
    }

    Set<String> uniqueInstruments = allocations.stream()
      .map(a -> a.instrumentId().value())
      .collect(Collectors.toSet());

    if (uniqueInstruments.size() != allocations.size()) {
      throw new BusinessRuleViolationException("Strategy cannot contain duplicate instruments");
    }

    BigDecimal allocationTotal = allocations.stream()
      .map(a -> a.percentage().value())
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal finalTotal = allocationTotal.add(cashReserve.percentage().value());

    if (finalTotal.compareTo(HUNDRED) != 0) {
      throw new BusinessRuleViolationException(
        "Allocations plus cash reserve must equal 100.00"
      );
    }

    allocations = List.copyOf(allocations);
  }
}
