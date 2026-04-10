package br.com.pauloviniciusdeveloper.finance.user.service;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.mapper.UserMapper;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Override
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }
}
