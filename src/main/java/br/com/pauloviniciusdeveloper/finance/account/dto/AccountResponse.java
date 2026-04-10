package br.com.pauloviniciusdeveloper.finance.account.dto;

import java.util.UUID;

import lombok.Builder;

@Builder
public record AccountResponse(
    UUID id,
    String name
) {}
