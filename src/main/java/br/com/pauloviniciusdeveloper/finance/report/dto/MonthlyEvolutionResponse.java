package br.com.pauloviniciusdeveloper.finance.report.dto;

import java.math.BigDecimal;

public record MonthlyEvolutionResponse(
    int month,
    String monthName,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance
) {}
