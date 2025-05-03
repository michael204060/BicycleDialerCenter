package com.bikedc.service.impl;

import com.bikedc.cache.BicycleCache;
import com.bikedc.dao.BicycleDao;
import com.bikedc.dao.UserBicycleDao;
import com.bikedc.dao.UserDao;
import com.bikedc.dto.UserBicycleDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        } else if (brand != null && model != null) {
            return bicycleDao.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(brand, model);
        } else if (brand != null) {
            return bicycleDao.findByBrandContainingIgnoreCase(brand);
        } else {
            return bicycleDao.findByModelContainingIgnoreCase(model);
        }
    }

    @Override
    public List<Bicycle> getBicyclesByAssignedUser(Long userId) {
        List<Bicycle> bicycles = bicycleDao.findByAssignedUserId(userId);
        if (bicycles.isEmpty()) {
            throw new ResourceNotFoundException("No bicycles found for user with id " + userId);
        }
        return bicycles;
    }

    @Override
    public List<Bicycle> getBicyclesByAssignedUserAttributes(Long userId, String username, String email) {
        List<Bicycle> bicycles = bicycleDao.findByAssignedUserAttributes(userId, username, email);
        if (bicycles.isEmpty()) {
            throw new ResourceNotFoundException("No bicycles found with given user criteria");
        }
        return bicycles;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bicycle> getBicycleById(Long id) {
        Bicycle cachedBicycle = bicycleCache.get(id);
        if (cachedBicycle != null) {
            return Optional.of(cachedBicycle);
        }
        Optional<Bicycle> bicycle = bicycleDao.findById(id);
        bicycle.ifPresent(b -> bicycleCache.put(b.getId(), b));
        return bicycle;
    }

    @Override
    @Transactional
    public Bicycle createBicycle(Bicycle bicycle) {
        if (bicycle.getAssignedUser() != null && bicycle.getAssignedUser().getId() != null) {
            User assignedUser = userDao.findById(bicycle.getAssignedUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + bicycle.getAssignedUser().getId()));
            bicycle.setAssignedUser(assignedUser);
        } else {
            bicycle.setAssignedUser(null);
        }
        Bicycle createdBicycle = bicycleDao.save(bicycle);
        bicycleCache.put(createdBicycle.getId(), createdBicycle);
        return createdBicycle;
    }

    @Override
    @Transactional
    public List<Bicycle> createBicycles(List<Bicycle> bicycles) {
        if (bicycles == null || bicycles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = bicycles.stream()
                .map(Bicycle::getAssignedUser)
                .filter(java.util.Objects::nonNull)
                .map(User::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        java.util.Map<Long, User> usersMap = userIds.isEmpty() ? Collections.emptyMap() :
                userDao.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        return bicycles.stream()
                .map(bicycle -> {
                    if (bicycle.getAssignedUser() != null && bicycle.getAssignedUser().getId() != null) {
                        User assignedUser = usersMap.get(bicycle.getAssignedUser().getId());
                        if (assignedUser == null) {
                            throw new ResourceNotFoundException("User not found with id " + bicycle.getAssignedUser().getId() + " for bicycle " + bicycle.getModel());
                        }
                        bicycle.setAssignedUser(assignedUser);
                    } else {
                        bicycle.setAssignedUser(null);
                    }
                    Bicycle created = bicycleDao.save(bicycle);
                    bicycleCache.put(created.getId(), created);
                    return created;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public Bicycle updateBicycle(Bicycle bicycle) {
        Bicycle existingBicycle = bicycleDao.findById(bicycle.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + bicycle.getId()));

        existingBicycle.setBrand(bicycle.getBrand());
        existingBicycle.setModel(bicycle.getModel());
        existingBicycle.setType(bicycle.getType());
        existingBicycle.setPrice(bicycle.getPrice());

        if (bicycle.getAssignedUser() != null && bicycle.getAssignedUser().getId() != null) {
            User newAssignedUser = userDao.findById(bicycle.getAssignedUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + bicycle.getAssignedUser().getId()));
            existingBicycle.setAssignedUser(newAssignedUser);
        } else if (bicycle.getAssignedUser() == null || bicycle.getAssignedUser().getId() == null) {
            existingBicycle.setAssignedUser(null);
        }

        Bicycle updatedBicycle = bicycleDao.save(existingBicycle);
        bicycleCache.put(updatedBicycle.getId(), updatedBicycle);
        return updatedBicycle;
    }

    @Override
    @Transactional
    public List<Bicycle> updateBicycles(List<Bicycle> bicycles) {
        if (bicycles == null || bicycles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = bicycles.stream()
                .map(Bicycle::getAssignedUser)
                .filter(java.util.Objects::nonNull)
                .map(User::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        java.util.Map<Long, User> usersMap = userIds.isEmpty() ? Collections.emptyMap() :
                userDao.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        return bicycles.stream()
                .map(dto -> {
                    Bicycle existingBicycle = bicycleDao.findById(dto.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + dto.getId()));

                    existingBicycle.setBrand(dto.getBrand());
                    existingBicycle.setModel(dto.getModel());
                    existingBicycle.setType(dto.getType());
                    existingBicycle.setPrice(dto.getPrice());

                    if (dto.getAssignedUser() != null && dto.getAssignedUser().getId() != null) {
                        User newAssignedUser = usersMap.get(dto.getAssignedUser().getId());
                        if (newAssignedUser == null) {
                            throw new ResourceNotFoundException("User not found with id " + dto.getAssignedUser().getId() + " for bicycle " + dto.getModel());
                        }
                        existingBicycle.setAssignedUser(newAssignedUser);
                    } else {
                        existingBicycle.setAssignedUser(null);
                    }

                    Bicycle updated = bicycleDao.save(existingBicycle);
                    bicycleCache.put(updated.getId(), updated);
                    return updated;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public UserBicycleDTO rentBicycle(Long userId, Long bicycleId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        Bicycle bicycle = bicycleDao.findById(bicycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + bicycleId));

        Optional<UserBicycle> existingRental = userBicycleDao.findByUserIdAndBicycleId(userId, bicycleId);
        if (existingRental.isPresent() && existingRental.get().getRentEndTime() == null) {
            throw new IllegalStateException("Bicycle is already currently rented by this user.");
        }

        UserBicycle userBicycle = new UserBicycle(user, bicycle);
        userBicycle.setRentStartTime(LocalDateTime.now());

        UserBicycle savedRental = userBicycleDao.save(userBicycle);

        return new UserBicycleDTO(savedRental);
    }

    @Override
    @Transactional
    public UserBicycleDTO returnBicycle(Long userId, Long bicycleId) {
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(userId, bicycleId);
        UserBicycle userBicycle = userBicycleDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental record not found for user " + userId + " and bicycle " + bicycleId));

        if (userBicycle.getRentEndTime() != null) {
            throw new IllegalStateException("Bicycle has already been returned.");
        }

        userBicycle.setRentEndTime(LocalDateTime.now());
        UserBicycle updatedRental = userBicycleDao.save(userBicycle);

        return new UserBicycleDTO(updatedRental);
    }

    @Override
    @Transactional
    public void deleteBicycle(Long id) {
        if (!bicycleDao.existsById(id)) {
            throw new ResourceNotFoundException("Bicycle not found with id " + id);
        }
        bicycleDao.deleteById(id);
        bicycleCache.evict(id);
    }

    @Override
    @Transactional
    public Bicycle assignUserToBicycle(Long bicycleId, Long userId) {
        Bicycle bicycle = bicycleDao.findById(bicycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + bicycleId));

        Optional<UserBicycle> activeRental = userBicycleDao.findByBicycleId(bicycleId).stream()
                .filter(r -> r.getRentEndTime() == null)
                .findFirst();
        if (activeRental.isPresent()) {
            throw new IllegalStateException("Cannot assign user to a bicycle that is currently rented.");
        }

        if (bicycle.getAssignedUser() != null && bicycle.getAssignedUser().getId().equals(userId)) {
            return bicycle;
        }
        if (bicycle.getAssignedUser() != null && !bicycle.getAssignedUser().getId().equals(userId)) {
            throw new IllegalStateException("Bicycle already has a different assigned user. Unassign first.");
        }


        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        bicycle.setAssignedUser(user);
        Bicycle updatedBicycle = bicycleDao.save(bicycle);
        bicycleCache.put(updatedBicycle.getId(), updatedBicycle);

        return updatedBicycle;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBicycleDTO> getAllRentals() {
        List<UserBicycle> rentals = userBicycleDao.findAll();
        return rentals.stream()
                .map(UserBicycleDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBicycleDTO> getRentalsForUser(Long userId) {
        List<UserBicycle> rentals = userBicycleDao.findByUserId(userId);
        return rentals.stream()
                .map(UserBicycleDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBicycleDTO> getRentalsForBicycle(Long bicycleId) {
        List<UserBicycle> rentals = userBicycleDao.findByBicycleId(bicycleId);
        return rentals.stream()
                .map(UserBicycleDTO::new)
                .collect(Collectors.toList());
    }
}