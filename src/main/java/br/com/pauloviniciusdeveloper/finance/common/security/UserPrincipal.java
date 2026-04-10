package br.com.pauloviniciusdeveloper.finance.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.pauloviniciusdeveloper.finance.user.entity.User;

import java.util.Collection;
import java.util.UUID;

public record UserPrincipal(User user) implements UserDetails {

    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    @Override
    public String getPassword() {
        return user.getHashedPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    public UUID getId() {
        return user.getId();
    }
}
