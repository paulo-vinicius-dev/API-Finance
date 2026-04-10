package br.com.pauloviniciusdeveloper.finance.insight.dto;

public record InsightResponse(
    InsightType type,
    String message,
    String value
) {
    public enum InsightType {
        TOP_EXPENSE_CATEGORY,
        MONTHLY_COMPARISON,
        SAVINGS_RATE,
        BUDGET_EXCEEDED,
        BUDGET_WARNING,
        EXPENSE_INCREASE
    }
}
