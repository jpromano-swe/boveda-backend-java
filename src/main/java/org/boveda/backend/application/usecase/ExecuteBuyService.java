package org.boveda.backend.application.usecase;

import java.util.Objects;
import java.util.Optional;

import org.boveda.backend.application.command.ExecuteBuyCommand;
import org.boveda.backend.application.dto.ExecuteBuyResult;
import org.boveda.backend.domain.service.FeePolicy;
import org.boveda.backend.domain.service.MinimumOrderPolicy;
import org.boveda.backend.domain.vo.BrokerOrderId;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.in.ExecuteBuyUseCase;
import org.boveda.backend.ports.out.BrokerTradingPort;
import org.boveda.backend.ports.out.IdempotencyPort;

public class ExecuteBuyService implements ExecuteBuyUseCase {

  private final MinimumOrderPolicy minimumOrderPolicy;
  private final FeePolicy feePolicy;
  private final BrokerTradingPort brokerTradingPort;
  private final IdempotencyPort idempotencyPort;

  public ExecuteBuyService(
    MinimumOrderPolicy minimumOrderPolicy,
    FeePolicy feePolicy,
    BrokerTradingPort brokerTradingPort,
    IdempotencyPort idempotencyPort
  ) {
    this.minimumOrderPolicy = Objects.requireNonNull(minimumOrderPolicy);
    this.feePolicy = Objects.requireNonNull(feePolicy);
    this.brokerTradingPort = Objects.requireNonNull(brokerTradingPort);
    this.idempotencyPort = Objects.requireNonNull(idempotencyPort);
  }

  @Override
  public ExecuteBuyResult execute(ExecuteBuyCommand command) {
    Objects.requireNonNull(command, "command must not be null");

    String key = command.idempotencyKey();
    Objects.requireNonNull(key, "idempotencyKey must not be null");

    Optional<ExecuteBuyResult> stored = idempotencyPort.findByKey(key);
    if (stored.isPresent()) {
      return stored.get();
    }

    minimumOrderPolicy.check(command.grossAmount(), command.minimumOrderAmount());

    Money fee = feePolicy.calculateFee(command.grossAmount(), command.feeRate());
    Money net = feePolicy.calculateNetAmount(command.grossAmount(), command.feeRate());

    BrokerOrderId brokerOrderId = brokerTradingPort.placeBuyOrder(
      command.userId(),
      command.instrumentId(),
      net
    );

    ExecuteBuyResult result = new ExecuteBuyResult(brokerOrderId, fee, net);
    idempotencyPort.save(key, result);

    return result;
  }
}
