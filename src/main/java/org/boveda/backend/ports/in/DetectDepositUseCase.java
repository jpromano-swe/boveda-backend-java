package org.boveda.backend.ports.in;

import org.boveda.backend.application.command.DetectDepositCommand;
import org.boveda.backend.application.dto.DetectDepositResult;

public interface DetectDepositUseCase {
  DetectDepositResult execute(DetectDepositCommand command);
}
