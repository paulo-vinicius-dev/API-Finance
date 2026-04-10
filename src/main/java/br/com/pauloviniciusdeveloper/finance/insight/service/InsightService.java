package br.com.pauloviniciusdeveloper.finance.insight.service;

import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.insight.dto.InsightResponse;

public interface InsightService {
    List<InsightResponse> getInsights(UUID userId, int month, int year);
}
