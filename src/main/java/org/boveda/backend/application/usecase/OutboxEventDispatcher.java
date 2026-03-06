package org.boveda.backend.application.usecase;

import org.boveda.backend.domain.model.OutboxEvent;

public interface OutboxEventDispatcher {
  void dispatch(OutboxEvent event);
}
