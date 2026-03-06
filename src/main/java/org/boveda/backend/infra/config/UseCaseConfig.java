package org.boveda.backend.infra.config;

import org.boveda.backend.application.usecase.DetectDepositService;
import org.boveda.backend.application.usecase.DispatchOutboxService;
import org.boveda.backend.application.usecase.ListDepositsService;
import org.boveda.backend.application.usecase.OutboxEventDispatcher;
import org.boveda.backend.ports.in.DetectDepositUseCase;
import org.boveda.backend.ports.in.ListDepositsUseCase;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.boveda.backend.ports.out.JobQueuePort;
import org.boveda.backend.ports.out.OutboxRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
  @Bean
  public DetectDepositUseCase detectDepositUseCase(
    DepositEventRepository repository,
    JobQueuePort jobQueuePort
  ) {
    return new DetectDepositService(repository, jobQueuePort);
  }

  @Bean
  public ListDepositsUseCase listDepositsUseCase(DepositQueryRepository repository) {
    return new ListDepositsService(repository);
  }

  @Bean
  public DispatchOutboxService dispatchOutboxService(
    OutboxRepository outboxRepository,
    OutboxEventDispatcher dispatcher
  ) {
    return new DispatchOutboxService(outboxRepository, dispatcher, 100, 3, 30);
  }
}
