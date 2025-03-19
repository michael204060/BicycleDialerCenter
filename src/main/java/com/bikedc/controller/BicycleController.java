package com.bikedc.controller;

import com.bikedc.dto.BicycleDTO;
import com.bikedc.dto.BicycleResponseDTO;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import com.bikedc.service.BicycleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bicycles")
public class BicycleController {
    private final BicycleService bicycleService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public BicycleController(BicycleService bicycleService) {
        this.bicycleService = bicycleService;
    }

    @GetMapping
    public ResponseEntity<List<BicycleResponseDTO>> getAllBicycles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByBrandAndModel(brand, model);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BicycleResponseDTO> getBicycleById(@PathVariable Long id) {
        Bicycle bicycle = bicycleService.getBicycleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + id));
        return ResponseEntity.ok(new BicycleResponseDTO(bicycle));
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @PostMapping
    @Transactional
    public ResponseEntity<BicycleResponseDTO> createBicycle(@RequestBody BicycleDTO bicycleDTO) {
        Bicycle bicycle = new Bicycle();
        bicycle.setBrand(bicycleDTO.getBrand());
        bicycle.setModel(bicycleDTO.getModel());
        bicycle.setType(bicycleDTO.getType());
        bicycle.setPrice(bicycleDTO.getPrice());

        if (bicycleDTO.getOwnerId() != null) {
            try {
                User owner = entityManager.getReference(User.class, bicycleDTO.getOwnerId());
                bicycle.setOwner(owner);
            } catch (EntityNotFoundException e) {
                throw new ResourceNotFoundException("User not found with id " + bicycleDTO.getOwnerId());
            }
        }

        Bicycle createdBicycle = bicycleService.createBicycle(bicycle);
        return ResponseEntity.ok(new BicycleResponseDTO(createdBicycle));
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BicycleResponseDTO> updateBicycle(@PathVariable Long id,
                                                            @RequestBody BicycleDTO bicycleDTO) {
        Bicycle bicycle = bicycleService.getBicycleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + id));

        bicycle.setBrand(bicycleDTO.getBrand());
        bicycle.setModel(bicycleDTO.getModel());
        bicycle.setType(bicycleDTO.getType());
        bicycle.setPrice(bicycleDTO.getPrice());

        if (bicycleDTO.getOwnerId() != null) {
            try {
                User owner = entityManager.getReference(User.class, bicycleDTO.getOwnerId());
                bicycle.setOwner(owner);
            } catch (EntityNotFoundException e) {
                throw new ResourceNotFoundException("User not found with id " + bicycleDTO.getOwnerId());
            }
        }

        Bicycle updatedBicycle = bicycleService.updateBicycle(bicycle);
        return ResponseEntity.ok(new BicycleResponseDTO(updatedBicycle));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBicycle(@PathVariable Long id) {
        bicycleService.deleteBicycle(id);
        return ResponseEntity.noContent().build();
    }
}