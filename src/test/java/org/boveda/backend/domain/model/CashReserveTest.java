package org.boveda.backend.domain.model;

import org.boveda.backend.domain.vo.Percentage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CashReserveTest {

  @Test
  void createCashReserve() {
   CashReserve reserve = new CashReserve(Percentage.of("10.00"));

   assertEquals("10.00", reserve.percentage().value().toPlainString());
  }

  @Test
  void allowsZeroReserve(){
    CashReserve reserve = new CashReserve(Percentage.of("0.00"));

    assertEquals("0.00", reserve.percentage().value().toPlainString());
  }

  @Test
  void rejectNullPercentage(){
    assertThrows(NullPointerException.class, ()->new CashReserve(null));
  }
}
