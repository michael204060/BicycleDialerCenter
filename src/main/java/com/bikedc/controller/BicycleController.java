package com.bikedc.controller;

import com.bikedc.cache.BicycleCache;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bicycles")
public class BicycleController {
    private final BicycleService bicycleService;
    private final BicycleCache bicycleCache;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public BicycleController(BicycleService bicycleService, BicycleCache bicycleCache) {
        this.bicycleService = bicycleService;
        this.bicycleCache = bicycleCache;
    }

    @GetMapping
    public ResponseEntity<List<BicycleResponseDTO>> getAllBicycles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByBrandAndModel(brand, model);
        if (bicycles.isEmpty()) {
            throw new ResourceNotFoundException("No bicycles found with given criteria");
        }
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByOwnerAttributes(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String ownerEmail) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByOwnerAttributes(ownerId, ownerName, ownerEmail);
        if (bicycles.isEmpty()) {
            throw new ResourceNotFoundException("No bicycles found with given owner criteria");
        }
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByOwner(@PathVariable Long ownerId) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByOwner(ownerId);
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

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BicycleResponseDTO> updateBicycle(@PathVariable Long id, @RequestBody BicycleDTO bicycleDTO) {
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
        UserBicycle userBicycle = bicycleService.rentBicycle(userId, bicycleId);
        return ResponseEntity.ok(userBicycle);
    }

    @PostMapping("/{bicycleId}/return/{userId}")
    public ResponseEntity<UserBicycle> returnBicycle(@PathVariable Long bicycleId, @PathVariable Long userId) {
        UserBicycle userBicycle = bicycleService.returnBicycle(userId, bicycleId);
        return ResponseEntity.ok(userBicycle);
    }

    @PostMapping("/{bicycleId}/match/{ownerId}")
    @Transactional
    public ResponseEntity<BicycleResponseDTO> matchBicycleWithOwner(
            @PathVariable Long bicycleId,
            @PathVariable Long ownerId) {
        Bicycle bicycle = bicycleService.matchBicycleWithOwner(bicycleId, ownerId);
        return ResponseEntity.ok(new BicycleResponseDTO(bicycle));
    }

    @GetMapping("/cache-stats")
    public ResponseEntity<String> getCacheStats() {
        bicycleCache.logCacheStats();
        return ResponseEntity.ok("Cache statistics logged to console");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBicycle(@PathVariable Long id) {
        bicycleService.deleteBicycle(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<String> handleNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred: " + e.getMessage());
    }
}