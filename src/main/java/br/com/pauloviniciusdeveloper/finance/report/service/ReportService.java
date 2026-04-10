package br.com.pauloviniciusdeveloper.finance.report.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.report.dto.CategoryBreakdownResponse;
import br.com.pauloviniciusdeveloper.finance.report.dto.MonthlyEvolutionResponse;
import br.com.pauloviniciusdeveloper.finance.report.dto.SummaryResponse;

public interface ReportService {
    SummaryResponse getSummary(UUID userId, LocalDate startDate, LocalDate endDate);
    List<CategoryBreakdownResponse> getExpensesByCategory(UUID userId, LocalDate startDate, LocalDate endDate);
    List<MonthlyEvolutionResponse> getMonthlyEvolution(UUID userId, int year);
}
