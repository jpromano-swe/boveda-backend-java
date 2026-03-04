package org.boveda.backend.ports.out;

import org.boveda.backend.application.dto.ExecuteBuyResult;

import java.util.Optional;

public interface IdempotencyPort {

  /**
   * Atomically reserves a key for processing a specific request fingerprint.
   * <p>
   * If the key is already completed with the same fingerprint, returns REPLAY with stored result.
   * If the key was used with a different fingerprint, returns CONFLICT.
   * If the key is new, returns ACQUIRED and caller can proceed.
   */
  Reservation reserve(String key, String requestFingerprint);

  void storeResult(String key, String requestFingerprint, ExecuteBuyResult result);

  enum Status {
    ACQUIRED,
    REPLAY,
    CONFLICT
  }

  record Reservation(Status status, Optional<ExecuteBuyResult> storedResult) {

    public static Reservation acquired() {
      return new Reservation(Status.ACQUIRED, Optional.empty());
    }

    public static Reservation replay(ExecuteBuyResult storedResult) {
      return new Reservation(Status.REPLAY, Optional.of(storedResult));
    }

    public static Reservation conflict() {
      return new Reservation(Status.CONFLICT, Optional.empty());
    }
  }
}
