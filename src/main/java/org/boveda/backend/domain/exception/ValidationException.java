package org.boveda.backend.domain.exception;

public class ValidationException extends DomainException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
