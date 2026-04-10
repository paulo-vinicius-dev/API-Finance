package br.com.pauloviniciusdeveloper.finance.auth.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterRequest;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthRegisterResponse;
import br.com.pauloviniciusdeveloper.finance.auth.dto.AuthResponse;
import br.com.pauloviniciusdeveloper.finance.auth.dto.LoginRequest;
import br.com.pauloviniciusdeveloper.finance.common.security.JwtTokenService;
import br.com.pauloviniciusdeveloper.finance.common.security.UserDetailsServiceImpl;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.mapper.UserMapper;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;

import java.util.Set;
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

    @Override
    public AuthRegisterResponse register(AuthRegisterRequest authRegisterRequest) {
        if (userRepository.existsByEmail(authRegisterRequest.email())) {
            throw new IllegalArgumentException("Email already in use: " + authRegisterRequest.email());
        }

        log.info("password: " + authRegisterRequest.password());
        log.info("encoded password: " + passwordEncoder.encode(authRegisterRequest.password()));

        User user = UserMapper.toEntity(
            authRegisterRequest,
            passwordEncoder.encode(authRegisterRequest.password()), 
            Set.of(Role.USER)
        );

        return UserMapper.toRegisterResponse(userRepository.save(user));
    }
}
