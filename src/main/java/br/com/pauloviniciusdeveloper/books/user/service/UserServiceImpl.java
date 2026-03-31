package br.com.pauloviniciusdeveloper.books.user.service;

import br.com.pauloviniciusdeveloper.books.user.entity.User;
import br.com.pauloviniciusdeveloper.books.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
