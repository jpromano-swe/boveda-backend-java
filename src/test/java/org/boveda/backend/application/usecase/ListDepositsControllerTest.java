package org.boveda.backend.adapters.in.rest;

import org.boveda.backend.application.dto.DepositListItem;
import org.boveda.backend.application.dto.ListDepositsResult;
import org.boveda.backend.application.query.ListDepositsQuery;
import org.boveda.backend.domain.vo.Money;
import org.boveda.backend.ports.in.ListDepositsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ListDepositsController.class)
@Import(ApiExceptionHandler.class)
class ListDepositsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ListDepositsUseCase useCase;

  @Test
  void returnsItems() throws Exception {
    DepositListItem item = new DepositListItem(
      UUID.randomUUID(),
      "BINANCE",
      "dep-001",
      Money.ars("1000.00"),
      Instant.parse("2026-03-05T12:00:00Z"),
      Instant.parse("2026-03-05T12:00:01Z"),
      "corr-001"
    );

    when(useCase.execute(any(ListDepositsQuery.class)))
      .thenReturn(new ListDepositsResult(List.of(item)));

    mockMvc.perform(get("/api/v1/users/{userId}/deposits", UUID.randomUUID())
        .param("limit", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.items[0].source").value("BINANCE"))
      .andExpect(jsonPath("$.items[0].externalEventId").value("dep-001"))
      .andExpect(jsonPath("$.items[0].amountArs").value("1000.00"));
  }

  @Test
  void returns400ForInvalidUserId() throws Exception {
    mockMvc.perform(get("/api/v1/users/{userId}/deposits", "invalid-uuid"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }
}
