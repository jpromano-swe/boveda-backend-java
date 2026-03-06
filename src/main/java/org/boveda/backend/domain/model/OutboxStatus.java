package org.boveda.backend.domain.model;

public enum OutboxStatus {
  PENDING,
  PROCESSING,
  RETRY,
  SENT,
  DEAD
}
