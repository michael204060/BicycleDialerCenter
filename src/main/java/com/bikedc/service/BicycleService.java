package com.bikedc.service;

import com.bikedc.model.Bicycle;
import com.bikedc.model.UserBicycle;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface BicycleService {
    @Transactional
    Bicycle createBicycle(Bicycle bicycle);

    @Transactional
    Bicycle updateBicycle(Bicycle bicycle);

    List<Bicycle> getBicyclesByBrandAndModel(String brand, String model);

    Optional<Bicycle> getBicycleById(Long id);

    @Transactional
    void deleteBicycle(Long id);

    UserBicycle rentBicycle(Long userId, Long bicycleId);

    UserBicycle returnBicycle(Long userId, Long bicycleId);
}