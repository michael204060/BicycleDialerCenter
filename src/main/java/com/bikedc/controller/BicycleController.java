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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.stream.Collectors;

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

    @Operation(summary = "Get all bicycles", description = "Returns list of bicycles with optional filtering by brand and model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<BicycleResponseDTO>> getAllBicycles(
            @Parameter(description = "Brand filter") @RequestParam(required = false) String brand,
            @Parameter(description = "Model filter") @RequestParam(required = false) String model
    ) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByBrandAndModel(brand, model);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get bicycles by owner ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByOwner(
            @Parameter(description = "ID of the owner") @PathVariable Long ownerId) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByOwner(ownerId);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get bicycle by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the bicycle"),
            @ApiResponse(responseCode = "404", description = "Bicycle not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BicycleResponseDTO> getBicycleById(
            @Parameter(description = "ID of bicycle to be retrieved") @PathVariable Long id) {
        Bicycle bicycle = bicycleService.getBicycleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + id));
        return ResponseEntity.ok(new BicycleResponseDTO(bicycle));
    }

    @Operation(summary = "Create new bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    @PostMapping
    @Transactional
    public ResponseEntity<BicycleResponseDTO> createBicycle(
            @Parameter(description = "Bicycle data to create") @Valid @RequestBody BicycleDTO bicycleDTO) {
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

    @Operation(summary = "Create multiple bicycles at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycles created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Owner not found for one of bicycles")
    })
    @PostMapping("/bulk")
    @Transactional
    public ResponseEntity<List<BicycleResponseDTO>> createBicycles(
            @Parameter(description = "List of bicycle data to create") @Valid @RequestBody List<BicycleDTO> bicycleDTOs) {
        List<Bicycle> bicycles = bicycleDTOs.stream()
                .map(dto -> {
                    Bicycle bicycle = new Bicycle();
                    bicycle.setBrand(dto.getBrand());
                    bicycle.setModel(dto.getModel());
                    bicycle.setType(dto.getType());
                    bicycle.setPrice(dto.getPrice());
                    if (dto.getOwnerId() != null) {
                        try {
                            User owner = entityManager.getReference(User.class, dto.getOwnerId());
                            bicycle.setOwner(owner);
                        } catch (EntityNotFoundException e) {
                            throw new ResourceNotFoundException("User not found with id " + dto.getOwnerId());
                        }
                    }
                    return bicycle;
                })
                .collect(Collectors.toList());

        List<Bicycle> createdBicycles = bicycleService.createBicycles(bicycles);
        List<BicycleResponseDTO> dtos = createdBicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Update bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Bicycle or owner not found")
    })
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BicycleResponseDTO> updateBicycle(
            @Parameter(description = "ID of bicycle to update") @PathVariable Long id,
            @Parameter(description = "Updated bicycle data") @Valid @RequestBody BicycleDTO bicycleDTO) {
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

    @Operation(summary = "Rent a bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle rented successfully"),
            @ApiResponse(responseCode = "404", description = "User or bicycle not found")
    })
    @PostMapping("/{bicycleId}/rent/{userId}")
    public ResponseEntity<UserBicycle> rentBicycle(
            @Parameter(description = "ID of bicycle to rent") @PathVariable Long bicycleId,
            @Parameter(description = "ID of user who rents") @PathVariable Long userId) {
        return ResponseEntity.ok(bicycleService.rentBicycle(userId, bicycleId));
    }

    @Operation(summary = "Return a bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle returned successfully"),
            @ApiResponse(responseCode = "404", description = "Rental record not found")
    })
    @PostMapping("/{bicycleId}/return/{userId}")
    public ResponseEntity<UserBicycle> returnBicycle(
            @Parameter(description = "ID of bicycle to return") @PathVariable Long bicycleId,
            @Parameter(description = "ID of user who returns") @PathVariable Long userId) {
        return ResponseEntity.ok(bicycleService.returnBicycle(userId, bicycleId));
    }

    @Operation(summary = "Delete a bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bicycle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bicycle not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBicycle(
            @Parameter(description = "ID of bicycle to delete") @PathVariable Long id) {
        bicycleService.deleteBicycle(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred: " + e.getMessage());
    }
}