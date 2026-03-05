package org.boveda.backend.adapters.in.rest;

import jakarta.validation.Valid;
import org.boveda.backend.application.command.DetectDepositCommand;
import org.boveda.backend.application.dto.DetectDepositResult;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.in.DetectDepositUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deposits")
public class DetectDepositController {

  private final DetectDepositUseCase useCase;

  public DetectDepositController(DetectDepositUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping("/detect")
  public ResponseEntity<DetectDepositResponse> detect(@Valid @RequestBody DetectDepositRequest request) {
    DetectDepositCommand command = new DetectDepositCommand(
      UUID.fromString(request.userId()),
      request.source(),
      request.externalEventId(),
      Money.ars(request.amountArs()),
      Instant.parse(request.occurredAt()),
      request.correlationId()
    );

    DetectDepositResult result = useCase.execute(command);

    HttpStatus status = result.status() == DetectDepositResult.Status.CREATED
      ? HttpStatus.CREATED
      : HttpStatus.OK;

    return ResponseEntity.status(status)
      .body(new DetectDepositResponse(result.status().name()));
  }
}
