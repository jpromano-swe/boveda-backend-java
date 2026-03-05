package org.boveda.backend.adapters.in.rest;

import org.boveda.backend.application.dto.DepositListItem;
import org.boveda.backend.application.dto.ListDepositsResult;
import org.boveda.backend.application.query.ListDepositsQuery;
import org.boveda.backend.ports.in.ListDepositsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/deposits")
public class ListDepositsController {

  private final ListDepositsUseCase useCase;

  public ListDepositsController(ListDepositsUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public ListDepositsResponse list(
    @PathVariable String userId,
    @RequestParam(required = false) String from,
    @RequestParam(required = false) String to,
    @RequestParam(defaultValue = "50") int limit
  ) {
    ListDepositsQuery query = new ListDepositsQuery(
      UUID.fromString(userId),
      from == null || from.isBlank() ? null : Instant.parse(from),
      to == null || to.isBlank() ? null : Instant.parse(to),
      limit
    );

    ListDepositsResult result = useCase.execute(query);

    List<DepositItemResponse> items = result.items().stream()
      .map(this::toResponse)
      .toList();

    return new ListDepositsResponse(items);
  }

  private DepositItemResponse toResponse(DepositListItem item) {
    return new DepositItemResponse(
      item.eventId().toString(),
      item.source(),
      item.externalEventId(),
      item.amount().amount().toPlainString(),
      item.occurredAt().toString(),
      item.detectedAt().toString(),
      item.correlationId()
    );
  }
}
