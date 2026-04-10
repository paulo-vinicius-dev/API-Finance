package br.com.pauloviniciusdeveloper.finance.auth.service;

import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterRequest;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterResponse;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthResponse;
import br.com.pauloviniciusdeveloper.finance.auth.dto.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    AuthRegisterResponse register(AuthRegisterRequest authRegisterRequest);
}
