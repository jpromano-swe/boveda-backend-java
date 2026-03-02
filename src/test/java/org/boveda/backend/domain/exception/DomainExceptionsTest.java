package org.boveda.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DomainExceptionsTest {

  @Test
  void validationExceptionIsADomainException() {
   DomainException exception = new ValidationException("invalid data");

    assertInstanceOf(RuntimeException.class, exception);
    assertEquals("invalid data", exception.getMessage());
  }

  @Test
  void businessRulesViolationExceptionIsADomainException(){
    DomainException exception = new BusinessRuleViolationException("minimum amount not met");

    assertInstanceOf(RuntimeException.class, exception);
    assertEquals("minimum amount not met", exception.getMessage());
  }

  @Test
  void domainExceptionCanWrapCause(){
    RuntimeException cause = new RuntimeException("cause");
    DomainException exception = new DomainException("wrapped", cause);

    assertEquals("wrapped", exception.getMessage());
    assertSame(cause, exception.getCause());
  }
}
