package org.boveda.backend.adapters.in.rest;

import java.util.List;

public record ListDepositsResponse(List<DepositItemResponse> items) { }
