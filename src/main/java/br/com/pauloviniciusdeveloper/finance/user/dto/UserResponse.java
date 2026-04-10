package br.com.pauloviniciusdeveloper.finance.user.dto;

import java.util.Set;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import lombok.Builder;

@Builder
public record UserResponse(
    UUID id,
    String fullName,
    String email,
    Set<Role> roles,
    boolean isActive
) {}
