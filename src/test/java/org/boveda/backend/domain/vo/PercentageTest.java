package org.boveda.backend.domain.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PercentageTest {

  @Test
  void createValidPercentage() {
    Percentage percentage = Percentage.of("2.50");

    assertEquals(new BigDecimal("2.50"), percentage.value());
  }

  @Test
  void rejectsNegativePercent() {
    assertThrows(IllegalArgumentException.class, () -> Percentage.of("-0.01"));
  }

  @Test
    void rejectsPercentGreaterThanHundred() {
      assertThrows(IllegalArgumentException.class, () -> Percentage.of("100.01"));
    }

  @Test
  void appliesPercentToMoney() {
    Percentage percentage = Percentage.of("2.50");
    Money amount = Money.ars("1000.00");

    Money result = percentage.applyTo(amount);

    assertEquals(Money.ars("25.00"), result);
  }

  @Test
  void appliesZeroPercentToMoney() {
    Percentage percentage = Percentage.of("0");
    Money amount = Money.ars("999.99");

    Money result = percentage.applyTo(amount);

    assertEquals(Money.ars("0.00"), result);
  }

  @Test
  void appliesHundredPercentToMoney() {
    Percentage percentage = Percentage.of("100.00");
    Money amount = Money.ars("123.45");

    Money result = percentage.applyTo(amount);

    assertEquals(Money.ars("123.45"), result);
  }

  @Test
  void roundsAppliedPercentageUsingHalfUp() {
    Percentage percentage = Percentage.of("1.25");
    Money amount = Money.ars("99.99");

    Money result = percentage.applyTo(amount);

    assertEquals(Money.ars("1.25"), result);
  }

  @Test
  void rejectsNullMoneyWhenApplyingPercent() {
    Percentage percentage = Percentage.of("10.00");

    assertThrows(NullPointerException.class, () -> percentage.applyTo(null));
  }

  @Test
  void normalizesPercentScaleToTwoDecimals() {
    Percentage percentage = Percentage.of("2");

    assertEquals(new BigDecimal("2.00"), percentage.value());
  }

}
