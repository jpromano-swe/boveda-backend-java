package org.boveda.backend.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money>{
  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

  public Money {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(currency, "currency must not be null");

    amount = amount.setScale(SCALE, ROUNDING);
  }

  public static Money ars(String amount) {
    return new Money(new BigDecimal(amount), Currency.getInstance("ARS"));
  }

  public static Money of(String amount, Currency currency) {
    return new Money(new BigDecimal(amount), currency);
  }

  public Money add(Money other) {
    ensureSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    ensureSameCurrency(other);
    return new Money(this.amount.subtract(other.amount), this.currency);
  }

  private void ensureSameCurrency(Money other) {
    Objects.requireNonNull(other, "other money must not be null");
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("Cannot operate on different currencies");
    }
  }

  public boolean isZero() {
    return amount.signum() == 0;
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  @Override
  public int compareTo(Money other) {
    ensureSameCurrency(other);
    return this.amount.compareTo(other.amount);
  }

}
