package org.boveda.backend.ports.in;

import org.boveda.backend.application.command.ReconcileTradesCommand;
import org.boveda.backend.application.dto.ReconcileTradesResult;

public interface ReconcileTradesUseCase {
  ReconcileTradesResult execute(ReconcileTradesCommand command);
}
