package org.boveda.backend.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Percent(BigDecimal value) {

  private static final BigDecimal ZERO = new BigDecimal("0.00");
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
  private static final BigDecimal HUNDRED_DIVISOR = new BigDecimal("100");
  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

  public Percent {
    Objects.requireNonNull(value, "value must not be null");

    value = value.setScale(SCALE, ROUNDING);

    if (value.compareTo(ZERO) < 0) {
      throw new IllegalArgumentException("Percent cannot be negative");
    }

    if (value.compareTo(ONE_HUNDRED) > 0) {
      throw new IllegalArgumentException("Percent cannot be greater than 100");
    }
  }

  public static Percent of(String value) {
    return new Percent(new BigDecimal(value));
  }

  public Money applyTo(Money money) {
    Objects.requireNonNull(money, "money must not be null");

    BigDecimal result = money.amount()
      .multiply(this.value)
      .divide(HUNDRED_DIVISOR, SCALE, ROUNDING);

    return new Money(result, money.currency());
  }
}
