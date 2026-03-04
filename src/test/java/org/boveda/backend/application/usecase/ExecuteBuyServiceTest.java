package org.boveda.backend.application.usecase;


import org.boveda.backend.application.command.ExecuteBuyCommand;
import org.boveda.backend.application.dto.ExecuteBuyResult;
import org.boveda.backend.domain.service.FeePolicy;
import org.boveda.backend.domain.service.MinimumOrderPolicy;
import org.boveda.backend.domain.service.RemainderPolicy;
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
      new RemainderPolicy(),
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

    when(idempotencyPort.reserve(eq("idem-001"), any())).thenReturn(IdempotencyPort.Reservation.acquired());
    when(brokerTradingPort.placeBuyOrder(userId, instrumentId, Money.ars("975.00")))
      .thenReturn(BrokerOrderId.of("ORD-123"));

    ExecuteBuyResult result = service.execute(command);

    assertEquals(BrokerOrderId.of("ORD-123"), result.brokerOrderId());
    assertEquals(Money.ars("25.00"), result.feeAmount());
    assertEquals(Money.ars("975.00"), result.netAmount());
    assertEquals(Money.ars("25.00"), result.remainderAmount());

    verify(brokerTradingPort).placeBuyOrder(userId, instrumentId, Money.ars("975.00"));
    verify(idempotencyPort).storeResult(eq("idem-001"), any(), eq(result));
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

    when(idempotencyPort.reserve(eq("idem-001"), any())).thenReturn(IdempotencyPort.Reservation.acquired());

    assertThrows(
      org.boveda.backend.domain.exception.BusinessRuleViolationException.class,
      () -> service.execute(command));

    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
    verify(idempotencyPort, never()).storeResult(any(), any(), any());
  }

  @Test
  void rejectsNullCommand(){
    assertThrows(NullPointerException.class, ()->service.execute(null));

    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
    verify(idempotencyPort, never()).reserve(any(), any());
    verify(idempotencyPort, never()).storeResult(any(), any(), any());
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
      Money.ars("975.00"),
      Money.ars("25.00")
    );

    when(idempotencyPort.reserve(eq("idem-001"), any()))
      .thenReturn(IdempotencyPort.Reservation.replay(stored));

    ExecuteBuyResult result = service.execute(command);

    assertEquals(stored, result);
    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
    verify(idempotencyPort,never()).storeResult(any(), any(), any());
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

    when(idempotencyPort.reserve(eq("idem-002"), any())).thenReturn(IdempotencyPort.Reservation.acquired());
    when(brokerTradingPort.placeBuyOrder(userId, instrumentId, Money.ars("975.00")))
      .thenReturn(BrokerOrderId.of("ORD-NEW"));

    ExecuteBuyResult result = service.execute(command);

    assertEquals(BrokerOrderId.of("ORD-NEW"), result.brokerOrderId());
    assertEquals(Money.ars("25.00"), result.feeAmount());
    assertEquals(Money.ars("975.00"), result.netAmount());
    assertEquals(Money.ars("25.00"), result.remainderAmount());

    verify(idempotencyPort).storeResult(eq("idem-002"), any(), eq(result));
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
    verify(idempotencyPort, never()).storeResult(any(), any(), any());
  }

  @Test
  void rejectsBlankIdempotencyKey(){
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
      "   "
    );

    assertThrows(org.boveda.backend.domain.exception.ValidationException.class, ()->service.execute(command));
    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
    verify(idempotencyPort, never()).reserve(any(), any());
    verify(idempotencyPort, never()).storeResult(any(), any(), any());
  }

  @Test
  void rejectsIdempotencyKeyReuseWithDifferentPayload() {
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
      "idem-conflict"
    );

    when(idempotencyPort.reserve(eq("idem-conflict"), any()))
      .thenReturn(IdempotencyPort.Reservation.conflict());

    assertThrows(org.boveda.backend.domain.exception.ValidationException.class, () -> service.execute(command));
    verify(brokerTradingPort, never()).placeBuyOrder(any(), any(), any());
    verify(idempotencyPort, never()).storeResult(any(), any(), any());
  }

}
