package org.boveda.backend.domain.service;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.Money;

import java.util.Objects;

public class MinimumOrderPolicy {

  public boolean isSatisfiedBy(Money amount, Money minimum) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(minimum, "minimum must not be null");
    return amount.compareTo(minimum) >= 0;
  }

  public void check (Money amount, Money minimum){
    if (!isSatisfiedBy(amount, minimum)) {
      throw new BusinessRuleViolationException(
        "Amount" + amount.amount() + " is  below minimum " + minimum.amount()
      );
    }
  }
}
