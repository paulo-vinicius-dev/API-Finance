package br.com.pauloviniciusdeveloper.finance.recurring.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.recurring.entity.Frequency;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecurringRequest(
    @NotNull(message = "Categoria é obrigatória")
    UUID categoryId,

    @NotNull(message = "Conta é obrigatória")
    UUID accountId,

    @NotNull(message = "Tipo é obrigatório")
    TransactionType type,

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    BigDecimal amount,

    @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
    String description,

    @NotNull(message = "Frequência é obrigatória")
    Frequency frequency,

    @NotNull(message = "Data de início é obrigatória")
    LocalDate startDate
) {}
