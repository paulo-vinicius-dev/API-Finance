package br.com.pauloviniciusdeveloper.finance.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryBreakdownResponse(
    UUID categoryId,
    String categoryName,
    BigDecimal total,
    BigDecimal percentage
) {}
