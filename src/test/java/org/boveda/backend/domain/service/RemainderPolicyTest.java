package org.boveda.backend.domain.service;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RemainderPolicyTest {

  private final RemainderPolicy policy = new RemainderPolicy();

  @Test
  void calculatesPositiveRemainder() {
    Money total = Money.ars("1000.00");
    Money used = Money.ars("450.00");

    Money remainder = policy.calculateRemainder(total,used);

    assertEquals(Money.ars("550.00"), remainder);
  }

  @Test
  void calculatesZeroRemainder(){
    Money total = Money.ars("1000.00");
    Money used = Money.ars("1000.00");

    Money remainder = policy.calculateRemainder(total,used);

    assertEquals(Money.ars("0.00"), remainder);
  }

  @Test
  void rejectsNegativeRemainder(){
    Money total = Money.ars("1000.00");
    Money used = Money.ars("1100.01");

    assertThrows(BusinessRuleViolationException.class, ()->policy.calculateRemainder(total,used));
  }

  @Test
  void checkDoesNotThrowForValidRemainder(){
    Money remainder = Money.ars("0.01");

    assertDoesNotThrow(()->policy.check(remainder));
  }

  @Test
  void checkThrowsForNegativeRemainder() {
    Money remainder = Money.ars("-0.01");

    assertThrows(BusinessRuleViolationException.class, () -> policy.check(remainder));
  }

  @Test
  void rejectsNullInputs() {
    Money total = Money.ars("1000.00");
    Money used = Money.ars("500.00");

    assertThrows(NullPointerException.class, () -> policy.calculateRemainder(null, used));
    assertThrows(NullPointerException.class, () -> policy.calculateRemainder(total, null));
    assertThrows(NullPointerException.class, () -> policy.check(null));
  }

}
