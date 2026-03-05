package org.boveda.backend.application.usecase;

import org.boveda.backend.application.dto.DepositListItem;
import org.boveda.backend.application.dto.ListDepositsResult;
import org.boveda.backend.application.query.ListDepositsQuery;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.model.DepositEvent;
import org.boveda.backend.ports.in.ListDepositsUseCase;
import org.boveda.backend.ports.out.DepositQueryRepository;
import java.util.Objects;
import java.util.List;

public class ListDepositsService implements ListDepositsUseCase {

  private final DepositQueryRepository repository;

  public ListDepositsService(DepositQueryRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public ListDepositsResult execute(ListDepositsQuery query) {
    Objects.requireNonNull(query, "query must not be null");

    if (query.userId() == null) {
      throw new ValidationException("userId must not be null");
    }

    if (query.limit() <= 0 || query.limit() > 500) {
      throw new ValidationException("limit must be between 1 and 500");
    }

    if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
      throw new ValidationException("from must be before or equal to to");
    }

    List<DepositEvent> events = repository.findByUserIdAndOccurredAtBetween(
      query.userId(),
      query.from(),
      query.to(),
      query.limit()
    );

    List<DepositListItem> items = events.stream()
      .map(e -> new DepositListItem(
        e.eventId(),
        e.source(),
        e.externalEventId(),
        e.amount(),
        e.occurredAt(),
        e.detectedAt(),
        e.correlationId()
      ))
      .toList();

    return new ListDepositsResult(items);
  }
}
