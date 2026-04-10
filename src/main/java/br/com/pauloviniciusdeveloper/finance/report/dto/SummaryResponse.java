package br.com.pauloviniciusdeveloper.finance.report.dto;

import java.math.BigDecimal;

public record SummaryResponse(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance,
    BigDecimal savingsRate
) {}
