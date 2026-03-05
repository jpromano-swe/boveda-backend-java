package org.boveda.backend.adapters.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.boveda.backend.application.command.DetectDepositCommand;
import org.boveda.backend.application.dto.DetectDepositResult;
import org.boveda.backend.domain.exception.ValidationException;
import org.boveda.backend.ports.in.DetectDepositUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DetectDepositController.class)
class DetectDepositControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private DetectDepositUseCase detectDepositUseCase;

  @Test
  void returns201WhenCreated() throws Exception {
    when(detectDepositUseCase.execute(any(DetectDepositCommand.class)))
      .thenReturn(DetectDepositResult.created());

    String body = """
      {
        "userId": "7f3ddf8d-0618-4f0a-8bda-677f21686fd5",
        "source": "BINANCE",
        "externalEventId": "dep-001",
        "amountArs": "1000.00",
        "occurredAt": "2026-03-05T12:00:00Z",
        "correlationId": "corr-001"
      }
      """;

    mockMvc.perform(post("/api/v1/deposits/detect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void returns200WhenDuplicate() throws Exception {
    when(detectDepositUseCase.execute(any(DetectDepositCommand.class)))
      .thenReturn(DetectDepositResult.duplicate());

    String body = """
      {
        "userId": "7f3ddf8d-0618-4f0a-8bda-677f21686fd5",
        "source": "BINANCE",
        "externalEventId": "dep-001",
        "amountArs": "1000.00",
        "occurredAt": "2026-03-05T12:00:00Z"
      }
      """;

    mockMvc.perform(post("/api/v1/deposits/detect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("DUPLICATE"));
  }

  @Test
  void returns400WhenDomainValidationFails() throws Exception {
    when(detectDepositUseCase.execute(any(DetectDepositCommand.class)))
      .thenThrow(new ValidationException("source must not be blank"));

    String body = """
      {
        "userId": "7f3ddf8d-0618-4f0a-8bda-677f21686fd5",
        "source": " ",
        "externalEventId": "dep-001",
        "amountArs": "1000.00",
        "occurredAt": "2026-03-05T12:00:00Z"
      }
      """;

    mockMvc.perform(post("/api/v1/deposits/detect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void returns400WhenUserIdIsMissing() throws Exception {
    String body = """
    {
      "source": "BINANCE",
      "externalEventId": "dep-001",
      "amountArs": "1000.00",
      "occurredAt": "2026-03-05T12:00:00Z"
    }
    """;

    mockMvc.perform(post("/api/v1/deposits/detect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    verifyNoInteractions(detectDepositUseCase);
  }

  @Test
  void returns400WhenOccurredAtIsBlank() throws Exception {
    String body = """
    {
      "userId": "7f3ddf8d-0618-4f0a-8bda-677f21686fd5",
      "source": "BINANCE",
      "externalEventId": "dep-001",
      "amountArs": "1000.00",
      "occurredAt": ""
    }
    """;

    mockMvc.perform(post("/api/v1/deposits/detect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    verifyNoInteractions(detectDepositUseCase);
  }

  @Test
  void returns400WhenJsonIsMalformed() throws Exception {
    String malformed = """
    {"userId":"x", "source":
    """;

    mockMvc.perform(post("/api/v1/deposits/detect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(malformed))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }
}
