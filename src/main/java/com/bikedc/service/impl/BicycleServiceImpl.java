package com.bikedc.service.impl;

import com.bikedc.dao.BicycleDao;
import com.bikedc.model.Bicycle;
import com.bikedc.service.BicycleService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BicycleServiceImpl implements BicycleService {
    private final BicycleDao bicycleDao;

    @Autowired
    public BicycleServiceImpl(BicycleDao bicycleDao) {
        this.bicycleDao = bicycleDao;
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
    public Optional<Bicycle> getBicycleById(Long id) {
        return bicycleDao.findById(id);
    }

    @Override
    public Bicycle createBicycle(Bicycle bicycle) {
        return bicycleDao.save(bicycle);
    }

    @Override
    public Bicycle updateBicycle(Bicycle bicycle) {
        return bicycleDao.save(bicycle);
    }

    @Override
    public void deleteBicycle(Long id) {
        bicycleDao.deleteById(id);
    }
}