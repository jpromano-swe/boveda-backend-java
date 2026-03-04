package org.boveda.backend.application.usecase;


import org.boveda.backend.application.command.ExecuteBuyCommand;
import org.boveda.backend.application.dto.ExecuteBuyResult;
import org.boveda.backend.domain.service.FeePolicy;
import org.boveda.backend.domain.service.MinimumOrderPolicy;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.domain.vo.Percentage;
import org.boveda.backend.ports.out.BrokerTradingPort;
import org.boveda.backend.ports.out.IdempotencyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExecuteBuyServiceTest {

  @Mock
  private BrokerTradingPort brokerTradingPort;

  @Mock
  private IdempotencyPort idempotencyPort;

  private ExecuteBuyService service;


  @BeforeEach
  void setUp(){
    service = new ExecuteBuyService(
      new MinimumOrderPolicy(),
      new FeePolicy(),
      brokerTradingPort,
      idempotencyPort
    );
  }

  @Test
  void executesBuyAndReturnsAmounts(){
    UUID userId = UUID.randomUUID();
    UUID strategyId = UUID.randomUUID();
    InstrumentId instrumentId = InstrumentId.of("BTCUSDT");

    ExecuteBuyCommand command = new ExecuteBuyCommand(
      userId,
      strategyId,
      instrumentId,
      Money.ars("1000.00"),
      Money.ars("500.00"),
      Percentage.of("2.50"),
      "idem-001"
    );

    when(brokerTradingPort.placeBuyOrder(userId, instrumentId, Money.ars("975.00")))
      .thenReturn(BrokerOrderId.of("ORD-123"));

    ExecuteBuyResult result = service.execute(command);

    assertEquals(BrokerOrderId.of("ORD-123"), result.brokerOrderId());
    assertEquals(Money.ars("25.00"), result.feeAmount());
    assertEquals(Money.ars("975.00"), result.netAmount());

    verify(brokerTradingPort).placeBuyOrder(userId, instrumentId, Money.ars("975.00"));
  }


  @Test
  void throwsWhenGrossAmountIsBelowMinimum(){
    UUID userId = UUID.randomUUID();
    UUID strategyId = UUID.randomUUID();
    InstrumentId instrumentId = InstrumentId.of("BTCUSDT");

    ExecuteBuyCommand command = new ExecuteBuyCommand(
      userId,
      strategyId,
      instrumentId,
      Money.ars("400.00"),
      Money.ars("500.00"),
      Percentage.of("2.50"),
      "idem-001"
    );

    assertThrows(
      org.boveda.backend.domain.exception.BusinessRuleViolationException.class,
      () -> service.execute(command));

    verify(brokerTradingPort, never()).placeBuyOrder(userId, instrumentId, Money.ars("487.49"));
  }

  @Test
  void rejectsNullCommand(){
    assertThrows(NullPointerException.class, ()->service.execute(null));

    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
  }

  @Test
  void returnsStoredResultWhenIdempotencyKeyAlreadyProcessed(){
    UUID userId = UUID.randomUUID();
    UUID strategyId = UUID.randomUUID();
    InstrumentId instrumentId = InstrumentId.of("BTCUSDT");

    ExecuteBuyCommand command = new ExecuteBuyCommand(
      userId,
      strategyId,
      instrumentId,
      Money.ars("1000.00"),
      Money.ars("500.00"),
      Percentage.of("2.50"),
      "idem-001"
    );

    ExecuteBuyResult stored = new ExecuteBuyResult(
      BrokerOrderId.of("ORD-STORED"),
      Money.ars("25.00"),
      Money.ars("975.00")
    );

    when(idempotencyPort.findByKey("idem-001")).thenReturn(java.util.Optional.of(stored));

    ExecuteBuyResult result = service.execute(command);

    assertEquals(stored, result);
    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
    verify(idempotencyPort,never()).save(any(),any());
  }

  @Test
  void storesResultWhenIdempotencyKeyIsNew(){
    UUID userId = UUID.randomUUID();
    UUID strategyId = UUID.randomUUID();
    InstrumentId instrumentId = InstrumentId.of("BTCUSDT");

    ExecuteBuyCommand command = new ExecuteBuyCommand(
      userId,
      strategyId,
      instrumentId,
      Money.ars("1000.00"),
      Money.ars("500.00"),
      Percentage.of("2.50"),
      "idem-002"
    );

    when(idempotencyPort.findByKey("idem-002")).thenReturn(java.util.Optional.empty());
    when(brokerTradingPort.placeBuyOrder(userId, instrumentId, Money.ars("975.00")))
      .thenReturn(BrokerOrderId.of("ORD-NEW"));

    ExecuteBuyResult result = service.execute(command);

    assertEquals(BrokerOrderId.of("ORD-NEW"), result.brokerOrderId());
    assertEquals(Money.ars("25.00"), result.feeAmount());
    assertEquals(Money.ars("975.00"), result.netAmount());

    verify(idempotencyPort).save("idem-002", result);
  }

  @Test
  void rejectsNullIdempotencyKey(){
    UUID userId = UUID.randomUUID();
    UUID strategyId = UUID.randomUUID();
    InstrumentId instrumentId = InstrumentId.of("BTCUSDT");

    ExecuteBuyCommand command = new ExecuteBuyCommand(
      userId,
      strategyId,
      instrumentId,
      Money.ars("1000.00"),
      Money.ars("500.00"),
      Percentage.of("2.50"),
      null
    );

    assertThrows(NullPointerException.class, ()->service.execute(command));
    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
  }

}
