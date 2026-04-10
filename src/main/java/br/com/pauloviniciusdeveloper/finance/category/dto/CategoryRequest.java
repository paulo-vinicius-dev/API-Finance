package br.com.pauloviniciusdeveloper.finance.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoryRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 30, message = "Category name must be at most 30 characters")
    String name
) {}
