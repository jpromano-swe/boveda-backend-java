package org.boveda.backend.ports.in;

import org.boveda.backend.application.command.ExecuteBuyCommand;
import org.boveda.backend.application.dto.ExecuteBuyResult;

public interface ExecuteBuyUseCase {
  ExecuteBuyResult execute(ExecuteBuyCommand command);
}
