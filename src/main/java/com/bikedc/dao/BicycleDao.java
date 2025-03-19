package com.bikedc.dao;

import com.bikedc.model.Bicycle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BicycleDao extends JpaRepository<Bicycle, Long> {
    List<Bicycle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model);

    List<Bicycle> findByBrandContainingIgnoreCase(String brand);

    List<Bicycle> findByModelContainingIgnoreCase(String model);

    List<Bicycle> findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(String brand, String model);
}