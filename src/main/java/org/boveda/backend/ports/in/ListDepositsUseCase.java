package org.boveda.backend.ports.in;

import org.boveda.backend.application.dto.ListDepositsResult;
import org.boveda.backend.application.query.ListDepositsQuery;

public interface ListDepositsUseCase {
  ListDepositsResult execute(ListDepositsQuery query);
}
