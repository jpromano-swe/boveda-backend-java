package org.boveda.backend.domain.vo;

import java.util.Objects;

public record BrokerOrderId (String value) {

  public BrokerOrderId {
    Objects.requireNonNull(value, "value must not be null");

    String normalized = value.trim();

    if (normalized.isBlank()) {
      throw new IllegalArgumentException("BrokerId cannot be blank");
    }
    value = normalized;
  }

  public static BrokerOrderId of(String value){
    return new BrokerOrderId(value);
  }
}
