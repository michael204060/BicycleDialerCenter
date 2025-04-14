package com.bikedc.service.impl;

import com.bikedc.dao.UserDao;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.User;
import com.bikedc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<User> getUsersByUsernameAndEmail(String username, String email) {
        List<User> users = userDao.findByUsernameOrEmail(username, email);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found with given criteria");
        }
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(userDao.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return userDao.save(user);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        if (!userDao.existsById(user.getId())) {
            throw new ResourceNotFoundException("User not found with id " + user.getId());
        }
        return userDao.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userDao.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        userDao.unlinkBicyclesFromUser(id);
        userDao.deleteById(id);
    }
}