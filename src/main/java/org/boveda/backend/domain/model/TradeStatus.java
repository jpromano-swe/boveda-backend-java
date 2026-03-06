package org.boveda.backend.domain.model;

public enum TradeStatus {
  PENDING,
  FILLED,
  CANCELED,
  REJECTED;

  public boolean isFinal(){
    return this == FILLED || this == CANCELED || this == REJECTED;
  }
}
