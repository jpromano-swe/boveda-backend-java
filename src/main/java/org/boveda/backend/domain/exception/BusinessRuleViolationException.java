package org.boveda.backend.domain.exception;

public class BusinessRuleViolationException extends DomainException {
  public BusinessRuleViolationException(String message) {
    super(message);
  }

  public BusinessRuleViolationException(String message, Throwable cause) {
    super(message, cause);
  }
}
