package br.com.pauloviniciusdeveloper.finance.user.dto;

import java.util.Set;

import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserAdminUpdateRequest(
    @NotNull(message = "isActive is required")
    Boolean isActive,

    @NotEmpty(message = "roles must not be empty")
    Set<Role> roles
) {}
