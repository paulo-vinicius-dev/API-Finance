package br.com.pauloviniciusdeveloper.finance.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {}
