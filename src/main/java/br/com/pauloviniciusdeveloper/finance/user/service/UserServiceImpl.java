package br.com.pauloviniciusdeveloper.finance.user.service;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserAdminUpdateRequest;
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

    @Override
    public Page<UserResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return userRepository.searchByNameOrEmail(search.trim(), pageable)
                .map(UserMapper::toResponse);
        }
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse adminUpdate(UUID id, UserAdminUpdateRequest request) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setIsActive(request.isActive());
        user.setRoles(request.roles());

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void adminDelete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}
