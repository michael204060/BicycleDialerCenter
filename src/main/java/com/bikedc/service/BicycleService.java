package com.bikedc.service;

import com.bikedc.model.Bicycle;
import com.bikedc.model.UserBicycle;
import java.util.List;
import java.util.Optional;

public interface BicycleService {
    List<Bicycle> getBicyclesByBrandAndModel(String brand, String model);
    List<Bicycle> getBicyclesByOwner(Long ownerId);
    List<Bicycle> getBicyclesByOwnerAttributes(Long ownerId, String ownerName, String ownerEmail);
    Optional<Bicycle> getBicycleById(Long id);
    Bicycle createBicycle(Bicycle bicycle);
    List<Bicycle> createBicycles(List<Bicycle> bicycles);
    Bicycle updateBicycle(Bicycle bicycle);
    List<Bicycle> updateBicycles(List<Bicycle> bicycles);
    UserBicycle rentBicycle(Long userId, Long bicycleId);
    UserBicycle returnBicycle(Long userId, Long bicycleId);
    void deleteBicycle(Long id);
    Bicycle matchBicycleWithOwner(Long bicycleId, Long ownerId);
}