package br.com.pauloviniciusdeveloper.finance.common.security;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.user.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.jwt.access-token-expiry-seconds:900}")
    private long accessTokenExpirySeconds;

    @Value("${app.jwt.refresh-token-expiry-seconds:604800}")
    private long refreshTokenExpirySeconds;

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpirySeconds, "access");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpirySeconds, "refresh");
    }

    private String buildToken(User user, long expirySeconds, String tokenType) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirySeconds);
        List<String> roleNames = user.getRoles().stream()
            .map(role -> role.name())
            .toList();

        log.debug("Generating {} token for userId={} with expiryAt={}",
            tokenType, user.getId(), expiresAt);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("books-api")
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("roles", roleNames)
            .claim("email", user.getEmail())
            .claim("token_type", tokenType)
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public UUID extractUserId(String token) {
        var jwt = jwtDecoder.decode(token);
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Extracted userId={} from token", userId);
        return userId;
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }
}
