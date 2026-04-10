package br.com.pauloviniciusdeveloper.finance.user.service;

import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;

public interface UserService {
    UserResponse findByEmail(String email);
    UserResponse findById(UUID id);
}
