package org.boveda.backend.infra.jobs;

import org.boveda.backend.application.usecase.DispatchOutboxResult;
import org.boveda.backend.application.usecase.DispatchOutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class OutboxDispatchScheduler {

  private static final Logger log = LoggerFactory.getLogger(OutboxDispatchScheduler.class);

  private final DispatchOutboxService dispatchOutboxService;

  public OutboxDispatchScheduler(DispatchOutboxService dispatchOutboxService) {
    this.dispatchOutboxService = Objects.requireNonNull(dispatchOutboxService);
  }

  @Scheduled(fixedDelayString = "${jobs.outbox.fixed-delay-ms:5000}")
  public void run() {
    DispatchOutboxResult result = dispatchOutboxService.dispatch(Instant.now());

    if ((result.sentCount() + result.retryCount() + result.deadCount() + result.skippedCount()) > 0) {
      log.info(
        "outbox_dispatch sent={} retry={} dead={} skipped={}",
        result.sentCount(),
        result.retryCount(),
        result.deadCount(),
        result.skippedCount()
      );
    }
  }
}
