package org.boveda.backend.infra.jobs;

import org.boveda.backend.application.usecase.OutboxEventDispatcher;
import org.boveda.backend.domain.model.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopOutboxEventDispatcher implements OutboxEventDispatcher {

  private static final Logger log = LoggerFactory.getLogger(NoopOutboxEventDispatcher.class);

  @Override
  public void dispatch(OutboxEvent event) {
    log.info("noop_outbox_dispatch eventType={} aggregateType={} aggregateId={}",
      event.eventType(), event.aggregateType(), event.aggregateId());
  }
}
