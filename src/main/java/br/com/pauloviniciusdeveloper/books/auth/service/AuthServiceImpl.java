package br.com.pauloviniciusdeveloper.books.auth.service;

import br.com.pauloviniciusdeveloper.books.auth.dto.AuthResponse;
import br.com.pauloviniciusdeveloper.books.auth.dto.LoginRequest;
import br.com.pauloviniciusdeveloper.books.common.security.JwtTokenService;
import br.com.pauloviniciusdeveloper.books.common.security.UserDetailsServiceImpl;
import br.com.pauloviniciusdeveloper.books.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.books.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login requested for email={}", maskEmail(request.email()));
        var userDetails = userDetailsService.loadUserByUsername(request.email());
        var userPrincipal = (UserPrincipal) userDetails;

        if (!passwordEncoder.matches(request.password(), userPrincipal.getPassword())) {
            log.warn("Login failed due to invalid credentials for email={}",
                maskEmail(request.email()));
            throw new UsernameNotFoundException("Invalid credentials");
        }

        var user = userPrincipal.user();
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        log.info("Login succeeded for userId={}", user.getId());

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtTokenService.getAccessTokenExpirySeconds()
        );
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        log.info("Refresh token requested");
        UUID userId = jwtTokenService.extractUserId(refreshToken);
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtTokenService.generateAccessToken(user);
        String newRefreshToken = jwtTokenService.generateRefreshToken(user);
        log.info("Refresh token succeeded for userId={}", userId);

        return new AuthResponse(
            newAccessToken,
            newRefreshToken,
            "Bearer",
            jwtTokenService.getAccessTokenExpirySeconds()
        );
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(at);
    }
}
