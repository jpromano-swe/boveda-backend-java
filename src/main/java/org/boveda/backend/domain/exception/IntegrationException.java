package org.boveda.backend.domain.exception;

public class IntegrationException extends DomainException {
  public IntegrationException(String message) {
    super(message);
  }

  public IntegrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
