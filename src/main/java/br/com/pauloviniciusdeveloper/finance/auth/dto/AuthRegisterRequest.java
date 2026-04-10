package br.com.pauloviniciusdeveloper.finance.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest (

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 16, message = "Password must be between 6 and 16 characters")
    String password
) {}
