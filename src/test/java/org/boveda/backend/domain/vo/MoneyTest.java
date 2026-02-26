package org.boveda.backend.domain.vo;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

  @Test
  void createArsMoneyWithScaleTwo() {
    Money money = Money.ars("100");

    assertEquals(new BigDecimal("100.00"), money.amount());
    assertEquals(Currency.getInstance("ARS"),money.currency());
  }

  @Test
  void addTwoAmountsWithSameCurrency(){
    Money left = Money.ars("100.25");
    Money right = Money.ars("49.75");

    Money result = left.add(right);

    assertEquals(new BigDecimal("150.00"), result.amount());
    assertEquals(Currency.getInstance("ARS"),result.currency());
  }

  @Test
  void subtractsTwoAmountsWithSameCurrency() {
    Money left = Money.ars("200.00");
    Money right = Money.ars("50.10");

    Money result = left.subtract(right);

    assertEquals(new BigDecimal("149.90"), result.amount());
    assertEquals(Currency.getInstance("ARS"), result.currency());
  }

  @Test
  void rejectsAddWhenCurrenciesDiffer() {
    Money ars = Money.ars("200.00");
    Money usd = Money.of("100.00", Currency.getInstance("USD"));

    assertThrows(IllegalArgumentException.class, () -> ars.add(usd));
  }
}
