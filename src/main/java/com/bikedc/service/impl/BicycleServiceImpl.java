package com.bikedc.service.impl;

import com.bikedc.dao.BicycleDao;
import com.bikedc.dao.UserBicycleDao;
import com.bikedc.dao.UserDao;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import com.bikedc.service.BicycleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BicycleServiceImpl implements BicycleService {
    private final BicycleDao bicycleDao;
    private final UserBicycleDao userBicycleDao;
    private final UserDao userDao;

    @Autowired
    public BicycleServiceImpl(BicycleDao bicycleDao, UserBicycleDao userBicycleDao, UserDao userDao) {
        this.bicycleDao = bicycleDao;
        this.userBicycleDao = userBicycleDao;
        this.userDao = userDao;
    }

    @Override
    public List<Bicycle> getBicyclesByBrandAndModel(String brand, String model) {
        if (brand == null) {
            brand = "";
        }
        if (model == null) {
            model = "";
        }
        return bicycleDao.findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(brand, model);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bicycle> getBicycleById(Long id) {
        return bicycleDao.findById(id);
    }

    @Override
    @Transactional
    public Bicycle createBicycle(Bicycle bicycle) {
        return bicycleDao.save(bicycle);
    }

    @Override
    @Transactional
    public Bicycle updateBicycle(Bicycle bicycle) {
        return bicycleDao.save(bicycle);
    }

    @Override
    @Transactional
    public void deleteBicycle(Long id) {
        bicycleDao.deleteById(id);
    }

    @Override
    @Transactional
    public UserBicycle rentBicycle(Long userId, Long bicycleId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        Bicycle bicycle = bicycleDao.findById(bicycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + bicycleId));

        UserBicycle userBicycle = new UserBicycle(user, bicycle);
        userBicycle.setRentStartTime(LocalDateTime.now());
        return userBicycleDao.save(userBicycle);
    }

    @Override
    @Transactional
    public UserBicycle returnBicycle(Long userId, Long bicycleId) {
        UserBicycle userBicycle = userBicycleDao.findById(new UserBicycle.UserBicycleId(userId, bicycleId))
                .orElseThrow(() -> new ResourceNotFoundException("Rent record not found for user" +
                        " " + userId + " and bicycle " + bicycleId));
        userBicycle.setRentEndTime(LocalDateTime.now());
        return userBicycleDao.save(userBicycle);
    }
}