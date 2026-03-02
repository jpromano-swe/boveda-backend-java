package org.boveda.backend.domain.model;

import org.boveda.backend.domain.exception.BusinessRuleViolationException;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Percentage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StrategyTest {

  @Test
  void createsValidStrategyWhenAllocationsPlusReserveEqualsHundred(){
    UUID strategyId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Allocation btc = new Allocation(InstrumentId.of("BTCUSDT"), Percentage.of("60.00"));
    Allocation eth = new Allocation(InstrumentId.of("ETHUSDT"), Percentage.of("30.00"));
    CashReserve reserve = new CashReserve(Percentage.of("10.00"));

    Strategy strategy = new Strategy(strategyId, userId, List.of(btc, eth), reserve);

    assertEquals(strategyId, strategy.strategyId());
    assertEquals(userId, strategy.userId());
    assertEquals(2, strategy.allocations().size());
    assertEquals("10.00", strategy.cashReserve().percentage().value().toPlainString());
  }

  @Test
  void rejectsEmptyAllocations(){
    assertThrows(BusinessRuleViolationException.class,
      ()-> new Strategy(UUID.randomUUID(), UUID.randomUUID(), List.of(), new CashReserve(Percentage.of("10.00")))
    );
  }

  @Test
  void rejectsDuplicateInstruments(){
    Allocation firstAllocation = new Allocation(InstrumentId.of("BTCUSDT"), Percentage.of("50.00"));
    Allocation secondAllocation = new Allocation(InstrumentId.of("BTCUSDT"), Percentage.of("40.00"));

    assertThrows(
      BusinessRuleViolationException.class,
      () -> new Strategy(UUID.randomUUID(), UUID.randomUUID(), List.of(firstAllocation, secondAllocation), new CashReserve(Percentage.of("10.00")))
    );
  }

  @Test
  void rejectsWhenTotalIsNotHundred(){
    Allocation btc = new Allocation(InstrumentId.of("BTCUSDT"), Percentage.of("60.00"));
    Allocation eth = new Allocation(InstrumentId.of("ETHUSDT"), Percentage.of("30.00"));
    CashReserve reserve = new CashReserve(Percentage.of("5.00"));

    assertThrows(
      BusinessRuleViolationException.class,
      ()-> new Strategy(UUID.randomUUID(), UUID.randomUUID(),List.of(btc,eth), reserve)
    );
  }
}
