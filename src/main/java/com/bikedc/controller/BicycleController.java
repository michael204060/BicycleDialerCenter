package com.bikedc.controller;

import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.UserBicycle;
import com.bikedc.service.BicycleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bicycles")
public class BicycleController {
    private final BicycleService bicycleService;

    @Autowired
    public BicycleController(BicycleService bicycleService) {
        this.bicycleService = bicycleService;
    }

    @GetMapping
    public ResponseEntity<List<Bicycle>> getAllBicycles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByBrandAndModel(brand, model);
        return
                ResponseEntity.ok(bicycles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bicycle> getBicycleById(@PathVariable Long id) {
        Bicycle bicycle = bicycleService.getBicycleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + id));
        return ResponseEntity.ok(bicycle);
    }

    @PostMapping
    public ResponseEntity<Bicycle> createBicycle(@RequestBody Bicycle bicycle) {
        Bicycle createdBicycle = bicycleService.createBicycle(bicycle);
        return ResponseEntity.ok(createdBicycle);
    }

    @PostMapping("/{bicycleId}/rent/{userId}")
    public ResponseEntity<UserBicycle> rentBicycle(@PathVariable Long bicycleId, @PathVariable Long userId) {
        return ResponseEntity.ok(bicycleService.rentBicycle(userId, bicycleId));
    }

    @PostMapping("/{bicycleId}/return/{userId}")
    public ResponseEntity<UserBicycle> returnBicycle(@PathVariable Long bicycleId,
                                                     @PathVariable Long userId) {
        return ResponseEntity.ok(bicycleService.returnBicycle(userId, bicycleId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bicycle> updateBicycle(@PathVariable Long id, @RequestBody Bicycle bicycleDetails) {
        Bicycle bicycle = bicycleService.getBicycleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + id));

        bicycle.setBrand(bicycleDetails.getBrand());
        bicycle.setModel(bicycleDetails.getModel());
        bicycle.setType(bicycleDetails.getType());
        bicycle.setPrice(bicycleDetails.getPrice());
        Bicycle updatedBicycle = bicycleService.updateBicycle(bicycle);
        return ResponseEntity.ok(updatedBicycle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBicycle(@PathVariable Long id) {
        bicycleService.deleteBicycle(id);
        return ResponseEntity.noContent().build();
    }
}