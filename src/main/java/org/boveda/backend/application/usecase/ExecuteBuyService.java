package org.boveda.backend.application.usecase;

import java.util.Objects;

import org.boveda.backend.application.command.ExecuteBuyCommand;
import org.boveda.backend.application.dto.ExecuteBuyResult;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.service.FeePolicy;
import org.boveda.backend.domain.service.MinimumOrderPolicy;
import org.boveda.backend.domain.service.RemainderPolicy;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.in.ExecuteBuyUseCase;
import org.boveda.backend.ports.out.BrokerTradingPort;
import org.boveda.backend.ports.out.IdempotencyPort;

public class ExecuteBuyService implements ExecuteBuyUseCase {

  private final MinimumOrderPolicy minimumOrderPolicy;
  private final FeePolicy feePolicy;
  private final RemainderPolicy remainderPolicy;
  private final BrokerTradingPort brokerTradingPort;
  private final IdempotencyPort idempotencyPort;

  public ExecuteBuyService(
    MinimumOrderPolicy minimumOrderPolicy,
    FeePolicy feePolicy,
    RemainderPolicy remainderPolicy,
    BrokerTradingPort brokerTradingPort,
    IdempotencyPort idempotencyPort
  ) {
    this.minimumOrderPolicy = Objects.requireNonNull(minimumOrderPolicy);
    this.feePolicy = Objects.requireNonNull(feePolicy);
    this.remainderPolicy = Objects.requireNonNull(remainderPolicy);
    this.brokerTradingPort = Objects.requireNonNull(brokerTradingPort);
    this.idempotencyPort = Objects.requireNonNull(idempotencyPort);
  }

  @Override
  public ExecuteBuyResult execute(ExecuteBuyCommand command) {
    Objects.requireNonNull(command, "command must not be null");

    String key = command.idempotencyKey();
    Objects.requireNonNull(key, "idempotencyKey must not be null");
    if (key.isBlank()) {
      throw new ValidationException("idempotencyKey must not be blank");
    }

    String requestFingerprint = fingerprintFor(command);

    IdempotencyPort.Reservation reservation = idempotencyPort.reserve(key, requestFingerprint);
    if (reservation.status() == IdempotencyPort.Status.REPLAY) {
      return reservation.storedResult()
        .orElseThrow(() -> new IllegalStateException("Missing stored result for replay"));
    }
    if (reservation.status() == IdempotencyPort.Status.CONFLICT) {
      throw new ValidationException("idempotencyKey already used with a different request");
    }

    minimumOrderPolicy.check(command.grossAmount(), command.minimumOrderAmount());

    Money fee = feePolicy.calculateFee(command.grossAmount(), command.feeRate());
    Money net = feePolicy.calculateNetAmount(command.grossAmount(), command.feeRate());
    Money remainder = remainderPolicy.calculateRemainder(command.grossAmount(), net);

    BrokerOrderId brokerOrderId = brokerTradingPort.placeBuyOrder(
      command.userId(),
      command.instrumentId(),
      net
    );

    ExecuteBuyResult result = new ExecuteBuyResult(brokerOrderId, fee, net, remainder);
    idempotencyPort.storeResult(key, requestFingerprint, result);

    return result;
  }

  private String fingerprintFor(ExecuteBuyCommand command) {
    return String.join("|",
      command.userId().toString(),
      command.strategyId().toString(),
      command.instrumentId().value(),
      command.grossAmount().amount().toPlainString(),
      command.minimumOrderAmount().amount().toPlainString(),
      command.feeRate().value().toPlainString()
    );
  }
}
