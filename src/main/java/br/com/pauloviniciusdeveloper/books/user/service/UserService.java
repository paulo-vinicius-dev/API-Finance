package br.com.pauloviniciusdeveloper.books.user.service;

import br.com.pauloviniciusdeveloper.books.user.entity.User;

public interface UserService {
    User findByEmail(String email);
    User save(User user);
}
