package org.boveda.backend.domain.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PercentageTest {

  @Test
  void createValidPercentage() {
    Percent percentage = Percent.of("2.50");

    assertEquals(new BigDecimal("2.50"), percentage.value());
  }

  @Test
  void rejectsNegativePercent() {
    assertThrows(IllegalArgumentException.class, () -> Percent.of("-0.01"));
  }

  @Test
    void rejectsPercentGreaterThanHundred() {
      assertThrows(IllegalArgumentException.class, () -> Percent.of("100.01"));
    }

  @Test
  void appliesPercentToMoney() {
    Percent percent = Percent.of("2.50");
    Money amount = Money.ars("1000.00");

    Money result = percent.applyTo(amount);

    assertEquals(Money.ars("25.00"), result);
  }

  @Test
  void appliesZeroPercentToMoney() {
    Percent percent = Percent.of("0");
    Money amount = Money.ars("999.99");

    Money result = percent.applyTo(amount);

    assertEquals(Money.ars("0.00"), result);
  }

  @Test
  void appliesHundredPercentToMoney() {
    Percent percent = Percent.of("100.00");
    Money amount = Money.ars("123.45");

    Money result = percent.applyTo(amount);

    assertEquals(Money.ars("123.45"), result);
  }

  @Test
  void roundsAppliedPercentageUsingHalfUp() {
    Percent percent = Percent.of("1.25");
    Money amount = Money.ars("99.99");

    Money result = percent.applyTo(amount);

    assertEquals(Money.ars("1.25"), result);
  }

  @Test
  void rejectsNullMoneyWhenApplyingPercent() {
    Percent percent = Percent.of("10.00");

    assertThrows(NullPointerException.class, () -> percent.applyTo(null));
  }

  @Test
  void normalizesPercentScaleToTwoDecimals() {
    Percent percent = Percent.of("2");

    assertEquals(new BigDecimal("2.00"), percent.value());
  }

}
