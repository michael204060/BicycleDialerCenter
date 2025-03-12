package com.bikedc.service;

import com.bikedc.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    List<User> getUsersByUsernameAndEmail(String username, String email);

    Optional<User> getUserById(Long id);

    @Transactional
    User createUser(User user);

    @Transactional
    User updateUser(User user);

    @Transactional
    void deleteUser(Long id);
}