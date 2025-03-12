package com.bikedc.service;

import com.bikedc.model.Bicycle;
import java.util.List;
import java.util.Optional;

public interface BicycleService {
    Bicycle createBicycle(Bicycle bicycle);

    Bicycle updateBicycle(Bicycle bicycle);

    List<Bicycle> getBicyclesByBrandAndModel(String brand, String model);

    Optional<Bicycle> getBicycleById(Long id);

    void deleteBicycle(Long id);
}