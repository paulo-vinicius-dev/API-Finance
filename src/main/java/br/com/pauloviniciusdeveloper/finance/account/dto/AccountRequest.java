package br.com.pauloviniciusdeveloper.finance.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AccountRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 20, message = "Name must be at most 20 characters")
    String name
) {}
