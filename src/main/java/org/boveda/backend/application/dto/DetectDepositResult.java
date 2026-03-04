package org.boveda.backend.application.dto;

public record DetectDepositResult(Status status) {
  public enum Status {
    CREATED,
    DUPLICATE
  }

  public static DetectDepositResult created() {
    return new DetectDepositResult(Status.CREATED);
  }

  public static DetectDepositResult duplicate() {
    return new DetectDepositResult(Status.DUPLICATE);
  }
}
