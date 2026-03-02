package org.boveda.backend.domain.model;

import org.boveda.backend.domain.vo.Percentage;

import java.util.Objects;

public record CashReserve(Percentage percentage) {

  public CashReserve {
    Objects.requireNonNull(percentage, "percentage must not be null");
  }
}
