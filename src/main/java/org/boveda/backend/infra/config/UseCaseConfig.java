package org.boveda.backend.infra.config;

import org.boveda.backend.application.usecase.DetectDepositService;
import org.boveda.backend.application.usecase.ListDepositsService;
import org.boveda.backend.ports.in.DetectDepositUseCase;
import org.boveda.backend.ports.in.ListDepositsUseCase;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
  @Bean
  public DetectDepositUseCase detectDepositUseCase(DepositEventRepository repository){
    return new DetectDepositService(repository);
  }

  @Bean
  public ListDepositsUseCase listDepositsUseCase(DepositQueryRepository repository) {
    return new ListDepositsService(repository);
  }
}
