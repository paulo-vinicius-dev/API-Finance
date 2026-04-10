package br.com.pauloviniciusdeveloper.finance.alert.dto;

import java.util.UUID;

public record AlertResponse(
    AlertType type,
    AlertSeverity severity,
    String message,
    UUID categoryId,
    String categoryName
) {
    public enum AlertType {
        BUDGET_EXCEEDED,
        BUDGET_WARNING,
        SPENDING_ABOVE_AVERAGE,
        SIGNIFICANT_EXPENSE_INCREASE
    }

    public enum AlertSeverity {
        INFO,
        WARNING,
        DANGER
    }
}
