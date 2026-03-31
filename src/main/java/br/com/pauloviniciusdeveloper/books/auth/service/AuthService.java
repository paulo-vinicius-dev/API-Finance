package br.com.pauloviniciusdeveloper.books.auth.service;

import br.com.pauloviniciusdeveloper.books.auth.dto.AuthResponse;
import br.com.pauloviniciusdeveloper.books.auth.dto.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
}
