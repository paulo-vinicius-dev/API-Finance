package br.com.pauloviniciusdeveloper.finance.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterRequest;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterResponse;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthResponse;
import br.com.pauloviniciusdeveloper.finance.auth.dto.LoginRequest;
import br.com.pauloviniciusdeveloper.finance.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e geração de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login de usuário")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> register(
        @Valid @RequestBody AuthRegisterRequest authRegisterRequest
    ) {
        return ResponseEntity.ok(authService.register(authRegisterRequest));
    }
    
}
