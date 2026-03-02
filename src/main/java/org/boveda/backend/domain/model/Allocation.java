package org.boveda.backend.domain.model;

import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.domain.vo.InstrumentId;
import org.boveda.backend.domain.vo.Percentage;

import java.util.Objects;

public record Allocation (InstrumentId instrumentId, Percentage percentage) {

  public Allocation {
    Objects.requireNonNull(instrumentId, "instrumentId must not be null");
    Objects.requireNonNull(percentage, "percentage must not be null");

    if (percentage.value().signum() == 0){
      throw new ValidationException("Percentage must be greater than zero");
    }
  }
}
