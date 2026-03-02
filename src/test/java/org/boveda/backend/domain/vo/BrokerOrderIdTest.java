package org.boveda.backend.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrokerOrderIdTest {

  @Test
  void createsBrokerOrderIdFromValidValue() {
    BrokerOrderId id = BrokerOrderId.of("123456789");

    assertEquals("123456789", id.value());
  }

  @Test
  void trimsValue(){
    BrokerOrderId id = BrokerOrderId.of("    abc-123   ");

    assertEquals("abc-123", id.value());
  }

  @Test
  void rejectsBlankValue(){
    assertThrows(IllegalArgumentException.class, ()-> BrokerOrderId.of("   "));
  }

  @Test
  void rejectsNullValue(){
    assertThrows(NullPointerException.class, ()-> BrokerOrderId.of(null));
  }
}
