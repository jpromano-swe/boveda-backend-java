package org.boveda.backend.domain.vo;

import java.util.Locale;
import java.util.Objects;

public record InstrumentId(String value) {
  public InstrumentId {
    Objects.requireNonNull(value, "value must not be null");

    String normalized = value.trim().toUpperCase(Locale.ROOT);

    if (normalized.isBlank()) {
      throw new IllegalArgumentException("InstrumentId cannot be blank");
    }

    if (normalized.contains(" ")){
      throw new IllegalArgumentException("InstrumentId cannot contain spaces");
    }

    value = normalized;
  }

  public static InstrumentId of(String value) {
    return new InstrumentId(value);
  }
}
