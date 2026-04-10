package br.com.pauloviniciusdeveloper.finance.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BudgetRequest(
    @NotNull(message = "Categoria é obrigatória")
    UUID categoryId,

    @NotNull(message = "Valor limite é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor limite deve ser maior que zero")
    BigDecimal limitAmount,

    @NotNull(message = "Mês é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    Integer month,

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2000, message = "Ano inválido")
    Integer year
) {}
