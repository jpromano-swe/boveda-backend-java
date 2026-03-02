package org.boveda.backend.domain.service;

import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.domain.vo.Percentage;
import java.util.Objects;

public class FeePolicy {

  public Money calculateFee(Money grossAmount, Percentage feeRate) {
    Objects.requireNonNull(grossAmount, "grossAmount must not be null");
    Objects.requireNonNull(feeRate, "feeRate must not be null");
    return feeRate.applyTo(grossAmount);
  }

  public Money calculateNetAmount(Money grossAmount, Percentage feeRate) {
    Money fee = calculateFee(grossAmount, feeRate);
    return grossAmount.subtract(fee);
  }
}
