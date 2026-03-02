package org.boveda.backend.domain.service;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinimumOrderPolicyTest {

  private final MinimumOrderPolicy policy = new MinimumOrderPolicy();

  @Test
  void returnsTrueWhenAmountIsGreaterThanMinimum() {
    Money amount = Money.ars("6000.00");
    Money minimum = Money.ars("5000.00");

    assertTrue(policy.isSatisfiedBy(amount, minimum));
  }

  @Test
  void returnsTrueWhenAmountIsEqualToMinimum() {
    Money amount = Money.ars("5000.00");
    Money minimum = Money.ars("5000.00");

    assertTrue(policy.isSatisfiedBy(amount, minimum));
  }

  @Test
  void returnsFalseWhenAmountIsLessThanMinimum() {
    Money amount = Money.ars("4999.99");
    Money minimum = Money.ars("5000.00");
  }

  @Test
  void throwsBusinessExceptionWhenAmountIsLowerThanMinimum() {
    Money amount = Money.ars("4999.99");
    Money minimum = Money.ars("5000.00");

    assertThrows(BusinessRuleViolationException.class, ()->policy.check(amount,minimum));
  }

  @Test
  void doesNotThrowWhenAmountMeetsMinimum(){
    Money amount = Money.ars("5000.00");
    Money minimum = Money.ars("5000.00");

    assertDoesNotThrow(() -> policy.check(amount, minimum));
  }

  @Test
  void rejectsNullAmount(){
    Money minimum = Money.ars("5000.00");

    assertThrows(NullPointerException.class, ()->policy.isSatisfiedBy(null,minimum));
  }

  @Test
  void rejectsNullMinimum(){
    Money amount = Money.ars("5000.00");

    assertThrows(NullPointerException.class, ()->policy.isSatisfiedBy(amount,null));
  }

}
