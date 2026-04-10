package br.com.pauloviniciusdeveloper.finance.common.security;

import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        Collection<GrantedAuthority> authorities = (roles == null ? List.<String>of() : roles)
            .stream()
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
            .toList();

        UUID userId = UUID.fromString(jwt.getSubject());
        var user = userRepository.findById(userId)
            .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        UserPrincipal principal = new UserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
