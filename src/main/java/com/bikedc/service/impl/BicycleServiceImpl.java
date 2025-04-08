package com.bikedc.service.impl;

import com.bikedc.cache.BicycleCache;
import com.bikedc.dao.BicycleDao;
import com.bikedc.dao.UserBicycleDao;
import com.bikedc.dao.UserDao;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import com.bikedc.service.BicycleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BicycleServiceImpl implements BicycleService {
    private final BicycleDao bicycleDao;
    private final UserBicycleDao userBicycleDao;
    private final UserDao userDao;
    private final BicycleCache bicycleCache;

    @Autowired
    public BicycleServiceImpl(BicycleDao bicycleDao,
                              UserBicycleDao userBicycleDao,
                              UserDao userDao,
                              BicycleCache bicycleCache) {
        this.bicycleDao = bicycleDao;
        this.userBicycleDao = userBicycleDao;
        this.userDao = userDao;
        this.bicycleCache = bicycleCache;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Bicycle> getBicyclesByBrandAndModel(String brand, String model) {
        if (brand == null && model == null) {
            return bicycleDao.findAll();
        }
        if (brand != null && model != null) {
            return bicycleDao.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(brand, model);
        } else if (brand != null) {
            return bicycleDao.findByBrandContainingIgnoreCase(brand);
        } else {
            return bicycleDao.findByModelContainingIgnoreCase(model);
        }
    }

    @Override
    public List<Bicycle> getBicyclesByOwner(Long ownerId) {
        return bicycleDao.findByOwnerId(ownerId);
    }

    @Override
    public List<Bicycle> getBicyclesByOwnerAttributes(Long ownerId, String ownerName, String ownerEmail) {
        return bicycleDao.findByOwnerAttributes(ownerId, ownerName, ownerEmail);
    }

    @Override
    public Optional<Bicycle> getBicycleById(Long id) {
        Bicycle cachedBicycle = bicycleCache.get(id);
        if (cachedBicycle != null) {
            return Optional.of(cachedBicycle);
        }
        Optional<Bicycle> bicycle = bicycleDao.findById(id);
        bicycle.ifPresent(b -> bicycleCache.put(id, b));
        return bicycle;
    }

    @Override
    @Transactional
    public Bicycle createBicycle(Bicycle bicycle) {
        if (bicycle.getOwner() != null) {
            bicycle.setOwner(entityManager.merge(bicycle.getOwner()));
        }
        Bicycle createdBicycle = bicycleDao.save(bicycle);
        bicycleCache.put(createdBicycle.getId(), createdBicycle);
        return createdBicycle;
    }

    @Override
    @Transactional
    public Bicycle updateBicycle(Bicycle bicycle) {
        if (bicycle.getOwner() != null) {
            bicycle.setOwner(entityManager.merge(bicycle.getOwner()));
        }
        Bicycle updatedBicycle = bicycleDao.save(bicycle);
        bicycleCache.put(updatedBicycle.getId(), updatedBicycle);
        return updatedBicycle;
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
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(userId, bicycleId);
        UserBicycle userBicycle = userBicycleDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental record not found for user " + userId + " and bicycle " + bicycleId));
        userBicycle.setRentEndTime(LocalDateTime.now());
        return userBicycleDao.save(userBicycle);
    }

    @Override
    @Transactional
    public void deleteBicycle(Long id) {
        bicycleDao.deleteById(id);
        bicycleCache.evict(id);
    }
}