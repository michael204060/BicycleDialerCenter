package com.bikedc.service;

import com.bikedc.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getUsersByUsernameAndEmail(String username, String email);

    Optional<User> getUserById(Long id);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);
}