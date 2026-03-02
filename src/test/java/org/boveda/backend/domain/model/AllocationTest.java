package org.boveda.backend.domain.model;

import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Percentage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AllocationTest {

  @Test
  void createValidAllocation() {
    Allocation allocation = new Allocation(InstrumentId.of("BTCUSDT"), Percentage.of("25.00"));

    assertEquals("BTCUSDT", allocation.instrumentId().value());
    assertEquals("25.00", allocation.percentage().value().toPlainString());
  }

  @Test
  void rejectsZeroPercentage(){
    assertThrows(
      ValidationException.class,
      ()-> new Allocation(InstrumentId.of("BTCUSDT"), Percentage.of("0.00"))
    );
  }

  @Test
  void rejectsNullInstrumentId(){
    assertThrows(NullPointerException.class, ()-> new Allocation(null, Percentage.of("10.00")));
  }

  @Test
  void rejectsNullPercentage(){
    assertThrows(NullPointerException.class, ()-> new Allocation(InstrumentId.of("BTCUSDT"), null));
  }
}
