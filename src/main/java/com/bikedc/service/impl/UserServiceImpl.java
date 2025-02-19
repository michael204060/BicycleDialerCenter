package com.bikedc.service.impl;

import com.bikedc.dao.UserDao;
import com.bikedc.model.User;
import com.bikedc.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<User> getUsersByUsernameAndEmail(String username, String email) {
        if (username == null) {
            username = "";
        }
        if (email == null) {
            email = "";
        }
        return userDao.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(username, email);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public User createUser(User user) {
        return userDao.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userDao.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }
}