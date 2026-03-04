package org.boveda.backend.ports.out;

import org.boveda.backend.domain.model.DepositEvent;

public interface DepositEventRepository {
  boolean existsBySourceAndExternalEventId(String source, String externalEventId);
  DepositEvent save(DepositEvent event);
}
