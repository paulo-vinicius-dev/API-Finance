package br.com.pauloviniciusdeveloper.finance.user.mapper;

import java.util.Set;

import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterRequest;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterResponse;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;

public class UserMapper {
    
    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .roles(user.getRoles())
            .isActive(user.getIsActive())
            .build();
    }

    public static AuthRegisterResponse toRegisterResponse(User user) {
        return AuthRegisterResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .roles(user.getRoles())
            .isActive(user.getIsActive())
            .build();
    }

    public static User toEntity(UserResponse userResponse) {
        return User.builder().id(userResponse.id())
            .fullName(userResponse.fullName())
            .email(userResponse.email())
            .roles(userResponse.roles())
            .isActive(userResponse.isActive())
            .build();
    }

    public static User toEntity(AuthRegisterRequest authRegisterRequest, String hashedPassword, Set<Role> roles) {
        return User.builder()
            .fullName(authRegisterRequest.fullName())
            .email(authRegisterRequest.email())
            .hashedPassword(hashedPassword)
            .roles(roles)
            .build();
    }
}
