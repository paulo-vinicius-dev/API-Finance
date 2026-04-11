package br.com.pauloviniciusdeveloper.finance.user.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.pauloviniciusdeveloper.finance.user.dto.UserAdminUpdateRequest;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;

public interface UserService {
    UserResponse findByEmail(String email);
    UserResponse findById(UUID id);

    // Admin-only operations
    Page<UserResponse> findAll(String search, Pageable pageable);
    UserResponse adminUpdate(UUID id, UserAdminUpdateRequest request);
    void adminDelete(UUID id);
}
