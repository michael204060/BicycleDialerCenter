package com.bikedc.service;

import com.bikedc.model.Bicycle;
import com.bikedc.dto.UserBicycleDTO;
import java.util.List;
import java.util.Optional;

public interface BicycleService {
    List<Bicycle> getBicyclesByBrandAndModel(String brand, String model);
    List<Bicycle> getBicyclesByAssignedUser(Long userId);
    List<Bicycle> getBicyclesByAssignedUserAttributes(Long userId, String username, String email);
    Optional<Bicycle> getBicycleById(Long id);
    Bicycle createBicycle(Bicycle bicycle);
    List<Bicycle> createBicycles(List<Bicycle> bicycles);
    Bicycle updateBicycle(Bicycle bicycle);
    List<Bicycle> updateBicycles(List<Bicycle> bicycles);
    void deleteBicycle(Long id);
    Bicycle assignUserToBicycle(Long bicycleId, Long userId);

    UserBicycleDTO rentBicycle(Long userId, Long bicycleId);
    UserBicycleDTO returnBicycle(Long userId, Long bicycleId);

    List<UserBicycleDTO> getAllRentals();
    List<UserBicycleDTO> getRentalsForUser(Long userId);
    List<UserBicycleDTO> getRentalsForBicycle(Long bicycleId);
}