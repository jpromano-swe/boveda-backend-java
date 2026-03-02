package org.boveda.backend.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InstrumentIdTest {

  @Test
  void createsInstrumentIdFromValidValue() {
    InstrumentId id = InstrumentId.of("BTCUSDT");

    assertEquals("BTCUSDT", id.value());
  }

  @Test
  void trimsAndNormalizesToUppercases() {
    InstrumentId id = InstrumentId.of("  btcusdt  ");

    assertEquals("BTCUSDT", id.value());
  }

  @Test
  void rejectsBlankValue() {
    assertThrows(IllegalArgumentException.class, () -> InstrumentId.of("   "));
  }

  @Test
  void rejectNullValue(){
    assertThrows(NullPointerException.class, () -> InstrumentId.of(null));
  }

  @Test
  void rejectsValueWithInternalSpaces() {
    assertThrows(IllegalArgumentException.class, () -> InstrumentId.of("BTC USDT"));
  }
}


