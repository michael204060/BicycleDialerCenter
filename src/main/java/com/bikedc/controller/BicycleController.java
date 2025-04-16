package com.bikedc.controller;

import com.bikedc.cache.BicycleCache;
import com.bikedc.dto.BicycleDTO;
import com.bikedc.dto.BicycleResponseDTO;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import com.bikedc.service.BicycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bicycles")
@Tag(name = "Bicycle Management", description = "Endpoints for managing bicycles")
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
    @Operation(summary = "Get all bicycles with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found bicycles"),
            @ApiResponse(responseCode = "404", description = "No bicycles found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> getAllBicycles(
            @Parameter(description = "Brand filter") @RequestParam(required = false) String brand,
            @Parameter(description = "Model filter") @RequestParam(required = false) String model) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByBrandAndModel(brand, model);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/owner")
    @Operation(summary = "Get bicycles by owner attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found bicycles"),
            @ApiResponse(responseCode = "404", description = "No bicycles found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByOwnerAttributes(
            @Parameter(description = "Owner ID") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "Owner name") @RequestParam(required = false) String ownerName,
            @Parameter(description = "Owner email") @RequestParam(required = false) String ownerEmail) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByOwnerAttributes(ownerId, ownerName, ownerEmail);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get bicycles by owner ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found bicycles"),
            @ApiResponse(responseCode = "404", description = "No bicycles found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByOwner(
            @Parameter(description = "Owner ID") @PathVariable Long ownerId) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByOwner(ownerId);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bicycle by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle found"),
            @ApiResponse(responseCode = "404", description = "Bicycle not found")
    })
    public ResponseEntity<BicycleResponseDTO> getBicycleById(
            @Parameter(description = "ID of bicycle to be fetched") @PathVariable Long id) {
        Bicycle bicycle = bicycleService.getBicycleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + id));
        return ResponseEntity.ok(new BicycleResponseDTO(bicycle));
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Create a new bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    public ResponseEntity<BicycleResponseDTO> createBicycle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Bicycle object to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BicycleDTO.class)))
            @Valid @RequestBody BicycleDTO bicycleDTO) {
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
    @Operation(summary = "Update a bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Bicycle or owner not found")
    })
    public ResponseEntity<BicycleResponseDTO> updateBicycle(
            @Parameter(description = "ID of bicycle to be updated") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated bicycle object",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BicycleDTO.class)))
            @Valid @RequestBody BicycleDTO bicycleDTO) {
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

    @PostMapping("/bulk")
    @Transactional
    @Operation(summary = "Create multiple bicycles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bicycles created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> createBicycles(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of bicycle objects to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BicycleDTO.class)))
            @Valid @RequestBody List<BicycleDTO> bicycleDTOs) {
        if (bicycleDTOs == null || bicycleDTOs.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

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

        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    @PutMapping("/bulk")
    @Transactional
    @Operation(summary = "Update multiple bicycles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycles updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Bicycle or owner not found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> updateBicycles(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of bicycle objects to be updated",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BicycleDTO.class)))
            @Valid @RequestBody List<BicycleDTO> bicycleDTOs) {
        if (bicycleDTOs == null || bicycleDTOs.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Bicycle> updatedBicycles = bicycleDTOs.stream()
                .map(dto -> {
                    Bicycle bicycle = bicycleService.getBicycleById(dto.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + dto.getId()));

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
                    } else {
                        bicycle.setOwner(null);
                    }

                    return bicycle;
                })
                .map(bicycleService::updateBicycle)
                .collect(Collectors.toList());

        List<BicycleResponseDTO> dtos = updatedBicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{bicycleId}/rent/{userId}")
    @Operation(summary = "Rent a bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle rented"),
            @ApiResponse(responseCode = "404", description = "User or bicycle not found")
    })
    public ResponseEntity<UserBicycle> rentBicycle(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId,
            @Parameter(description = "User ID") @PathVariable Long userId) {
        UserBicycle userBicycle = bicycleService.rentBicycle(userId, bicycleId);
        return ResponseEntity.ok(userBicycle);
    }

    @PostMapping("/{bicycleId}/return/{userId}")
    @Operation(summary = "Return a rented bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle returned"),
            @ApiResponse(responseCode = "404", description = "Rental record not found")
    })
    public ResponseEntity<UserBicycle> returnBicycle(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId,
            @Parameter(description = "User ID") @PathVariable Long userId) {
        UserBicycle userBicycle = bicycleService.returnBicycle(userId, bicycleId);
        return ResponseEntity.ok(userBicycle);
    }

    @PostMapping("/{bicycleId}/match/{ownerId}")
    @Transactional
    @Operation(summary = "Match bicycle with owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle matched with owner"),
            @ApiResponse(responseCode = "404", description = "Bicycle or owner not found"),
            @ApiResponse(responseCode = "409", description = "Bicycle already has an owner")
    })
    public ResponseEntity<BicycleResponseDTO> matchBicycleWithOwner(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId,
            @Parameter(description = "Owner ID") @PathVariable Long ownerId) {
        Bicycle bicycle = bicycleService.matchBicycleWithOwner(bicycleId, ownerId);
        return ResponseEntity.ok(new BicycleResponseDTO(bicycle));
    }

    @GetMapping("/cache-stats")
    @Operation(summary = "Get bicycle cache statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache statistics logged")
    })
    public ResponseEntity<String> getCacheStats() {
        bicycleCache.logCacheStats();
        return ResponseEntity.ok("Cache statistics logged to console");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bicycle by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bicycle deleted"),
            @ApiResponse(responseCode = "404", description = "Bicycle not found")
    })
    public ResponseEntity<Void> deleteBicycle(
            @Parameter(description = "ID of bicycle to be deleted") @PathVariable Long id) {
        bicycleService.deleteBicycle(id);
        return ResponseEntity.noContent().build();
    }
}