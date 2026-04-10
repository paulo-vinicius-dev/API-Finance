package br.com.pauloviniciusdeveloper.finance.category.dto;

import java.util.UUID;

import lombok.Builder;

@Builder
public record CategoryResponse(UUID id, String name, boolean isDefault) {}
