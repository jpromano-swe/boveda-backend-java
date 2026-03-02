package org.boveda.backend.domain.service;

import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.domain.vo.Percentage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeePolicyTest {

  private final FeePolicy feePolicy = new FeePolicy();

  @Test
  void calculatesFeeFromGrossAmount(){
    Money gross = Money.ars("1000.00");
    Percentage feeRate = Percentage.of("2.50");

    Money fee = feePolicy.calculateFee(gross, feeRate);

    assertEquals(Money.ars("25.00"), fee);
  }

  @Test
  void calculateNetAmountAfterFee(){
    Money gross = Money.ars("1000.00");
    Percentage feeRate = Percentage.of("2.50");

    Money net = feePolicy.calculateNetAmount(gross, feeRate);

    assertEquals(Money.ars("975.00"), net);
  }

  @Test
  void zeroFeeRateReturnsSameAmount(){
    Money gross = Money.ars("999.99");
    Percentage zero = Percentage.of("0.00");

    Money net = feePolicy.calculateNetAmount(gross, zero);

    assertEquals(Money.ars("999.99"), net);
  }

  @Test
  void rejectNullGrossAmount(){
    Percentage feeRate = Percentage.of("1.50");

    assertThrows(NullPointerException.class, ()->feePolicy.calculateFee(null, feeRate));
  }

  @Test
  void rejectNullFeeRate(){
    Money gross = Money.ars("1000.00");

    assertThrows(NullPointerException.class, ()->feePolicy.calculateFee(gross,null));
  }
}
