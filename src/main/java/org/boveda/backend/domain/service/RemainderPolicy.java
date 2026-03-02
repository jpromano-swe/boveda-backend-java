package org.boveda.backend.domain.service;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.Money;

import java.util.Objects;

public class RemainderPolicy {

  public Money calculateRemainder(Money totalAmount, Money usedAmount) {
    Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    Objects.requireNonNull(usedAmount, "usedAmount must not be null");

    Money remainder = totalAmount.subtract(usedAmount);

    check(remainder);

    return remainder;
  }

  public void check(Money remainder) {
    Objects.requireNonNull(remainder, "remainder must not be null");

    if(remainder.isNegative()) {
      throw new BusinessRuleViolationException("Remainder cannot be negative");
    }
  }
}
