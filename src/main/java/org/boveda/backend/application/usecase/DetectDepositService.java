package org.boveda.backend.application.usecase;

import org.boveda.backend.application.command.DetectDepositCommand;
import org.boveda.backend.application.dto.DetectDepositResult;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.ports.in.DetectDepositUseCase;
import org.boveda.backend.ports.out.DepositEventRepository;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class DetectDepositService implements DetectDepositUseCase {

  private final DepositEventRepository repository;

  public DetectDepositService(DepositEventRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public DetectDepositResult execute(DetectDepositCommand command){
    Objects.requireNonNull(command, "command must not be null");

    if (command.userId() == null){
      throw new ValidationException("userId must not be null");
    }

    if (command.source() == null || command.source().isBlank()){
      throw new ValidationException("source must not be blank");
    }

    if (command.externalEventId() == null || command.externalEventId().isBlank()) {
      throw new ValidationException("externalEventId must not be blank");
    }
    if (command.amount() == null || !command.amount().isPositive()) {
      throw new ValidationException("deposit amount must be positive");
    }

    if(command.occurredAt() == null){
      throw new ValidationException("occurredAt must not be null");
    }

    String normalizedSource = command.source().toUpperCase(Locale.ROOT);
    String normalizedExternalEventId = command.externalEventId().trim();
    String correlationId = resolveCorrelationId(command.correlationId());

    if (repository.existsBySourceAndExternalEventId(normalizedSource, normalizedExternalEventId)) {
      return DetectDepositResult.duplicate();
    }

    Instant now = Instant.now();

    DepositEvent event = new DepositEvent(
      UUID.randomUUID(),
      command.userId(),
      command.source(),
      command.externalEventId(),
      command.amount(),
      command.occurredAt(),
      now,
      correlationId,
      now,
      now
    );

    repository.save(event);
    return DetectDepositResult.created();
  }

  private String resolveCorrelationId(String correlationId) {
    if (correlationId == null || correlationId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return correlationId.trim();
  }
}
