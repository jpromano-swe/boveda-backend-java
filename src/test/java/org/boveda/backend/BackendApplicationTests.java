package org.boveda.backend;

import org.boveda.backend.application.usecase.OutboxEventDispatcher;
import org.boveda.backend.ports.out.DepositEventRepository;
import org.boveda.backend.ports.out.DepositQueryRepository;
import org.boveda.backend.ports.out.JobQueuePort;
import org.boveda.backend.ports.out.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    }
)
class BackendApplicationTests {

  @MockBean
  private DepositEventRepository depositEventRepository;

  @MockBean
  private DepositQueryRepository depositQueryRepository;

  @MockBean
  private JobQueuePort jobQueuePort;

  @MockBean
  private OutboxRepository outboxRepository;

  @MockBean
  private OutboxEventDispatcher outboxEventDispatcher;

  @Test
  void contextLoads() {
  }
}
