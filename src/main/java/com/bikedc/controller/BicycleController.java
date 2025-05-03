package com.bikedc.controller;

import com.bikedc.cache.BicycleCache;
import com.bikedc.dto.BicycleDTO;
import com.bikedc.dto.BicycleResponseDTO;
import com.bikedc.dto.UserBicycleDTO;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.exception.ErrorDetails;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import com.bikedc.service.BicycleService;
import com.bikedc.service.UserService;
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
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bicycles")
@Tag(name = "Bicycle Management", description = "Endpoints for managing bicycles and rentals")
public class BicycleController {
    private final BicycleService bicycleService;
    private final BicycleCache bicycleCache;
    private final UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public BicycleController(BicycleService bicycleService, BicycleCache bicycleCache, UserService userService) {
        this.bicycleService = bicycleService;
        this.bicycleCache = bicycleCache;
        this.userService = userService;
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

    @GetMapping("/user")
    @Operation(summary = "Get bicycles by assigned user attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found bicycles"),
            @ApiResponse(responseCode = "404", description = "No bicycles found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByAssignedUserAttributes(
            @Parameter(description = "Assigned User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Assigned User name") @RequestParam(required = false) String username,
            @Parameter(description = "Assigned User email") @RequestParam(required = false) String email) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByAssignedUserAttributes(userId, username, email);
        List<BicycleResponseDTO> dtos = bicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bicycles by assigned user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found bicycles"),
            @ApiResponse(responseCode = "404", description = "No bicycles found")
    })
    public ResponseEntity<List<BicycleResponseDTO>> getBicyclesByAssignedUser(
            @Parameter(description = "Assigned User ID") @PathVariable Long userId) {
        List<Bicycle> bicycles = bicycleService.getBicyclesByAssignedUser(userId);
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
            @ApiResponse(responseCode = "404", description = "Assigned user not found")
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

                    if (dto.getAssignedUserId() != null) {
                        try {
                            User assignedUser = entityManager.getReference(User.class, dto.getAssignedUserId());
                            bicycle.setAssignedUser(assignedUser);
                        } catch (EntityNotFoundException e) {
                            throw new ResourceNotFoundException("User not found with id " + dto.getAssignedUserId());
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

    @PostMapping("/single") // Changed endpoint name to avoid conflict with /bulk
    @Transactional
    @Operation(summary = "Create a single bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Assigned user not found")
    })
    public ResponseEntity<BicycleResponseDTO> createSingleBicycle( // Changed method name
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

        if (bicycleDTO.getAssignedUserId() != null) {
            try {
                User assignedUser = entityManager.getReference(User.class, bicycleDTO.getAssignedUserId());
                bicycle.setAssignedUser(assignedUser);
            } catch (EntityNotFoundException e) {
                throw new ResourceNotFoundException("User not found with id " + bicycleDTO.getAssignedUserId());
            }
        } else {
            bicycle.setAssignedUser(null);
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
            @ApiResponse(responseCode = "404", description = "Bicycle or assigned user not found")
    })
    public ResponseEntity<BicycleResponseDTO> updateBicycle(
            @Parameter(description = "ID of bicycle to be updated") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated bicycle object",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BicycleDTO.class)))
            @Valid @RequestBody BicycleDTO bicycleDTO) {

        Bicycle bicycleToUpdate = new Bicycle();
        bicycleToUpdate.setId(id);
        bicycleToUpdate.setBrand(bicycleDTO.getBrand());
        bicycleToUpdate.setModel(bicycleDTO.getModel());
        bicycleToUpdate.setType(bicycleDTO.getType());
        bicycleToUpdate.setPrice(bicycleDTO.getPrice());

        if (bicycleDTO.getAssignedUserId() != null) {
            try {
                User assignedUser = entityManager.getReference(User.class, bicycleDTO.getAssignedUserId());
                bicycleToUpdate.setAssignedUser(assignedUser);
            } catch (EntityNotFoundException e) {
                throw new ResourceNotFoundException("User not found with id " + bicycleDTO.getAssignedUserId());
            }
        } else {
            bicycleToUpdate.setAssignedUser(null);
        }

        Bicycle updatedBicycle = bicycleService.updateBicycle(bicycleToUpdate);
        return ResponseEntity.ok(new BicycleResponseDTO(updatedBicycle));
    }

    @PutMapping("/bulk")
    @Transactional
    @Operation(summary = "Update multiple bicycles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycles updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Bicycle or assigned user not found")
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

        List<Bicycle> bicyclesToUpdate = bicycleDTOs.stream()
                .map(dto -> {
                    Bicycle bicycle = new Bicycle();
                    bicycle.setId(dto.getId());
                    bicycle.setBrand(dto.getBrand());
                    bicycle.setModel(dto.getModel());
                    bicycle.setType(dto.getType());
                    bicycle.setPrice(dto.getPrice());
                    if (dto.getAssignedUserId() != null) {
                        try {
                            User assignedUser = entityManager.getReference(User.class, dto.getAssignedUserId());
                            bicycle.setAssignedUser(assignedUser);
                        } catch (EntityNotFoundException e) {
                            throw new ResourceNotFoundException("User not found with id " + dto.getAssignedUserId());
                        }
                    } else {
                        bicycle.setAssignedUser(null);
                    }
                    return bicycle;
                })
                .collect(Collectors.toList());


        List<Bicycle> updatedBicycles = bicycleService.updateBicycles(bicyclesToUpdate);
        List<BicycleResponseDTO> dtos = updatedBicycles.stream()
                .map(BicycleResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    @PostMapping("/{bicycleId}/rent/{userId}")
    @Operation(summary = "Rent a bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle rented", content = @Content(schema = @Schema(implementation = UserBicycleDTO.class))),
            @ApiResponse(responseCode = "404", description = "User or bicycle not found"),
            @ApiResponse(responseCode = "409", description = "Bicycle already rented by this user")
    })
    public ResponseEntity<UserBicycleDTO> rentBicycle(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId,
            @Parameter(description = "User ID") @PathVariable Long userId) {
        UserBicycleDTO userBicycleDTO = bicycleService.rentBicycle(userId, bicycleId);
        return ResponseEntity.ok(userBicycleDTO);
    }

    @PostMapping("/{bicycleId}/return/{userId}")
    @Operation(summary = "Return a rented bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bicycle returned", content = @Content(schema = @Schema(implementation = UserBicycleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Rental record not found"),
            @ApiResponse(responseCode = "409", description = "Bicycle already returned")
    })
    public ResponseEntity<UserBicycleDTO> returnBicycle(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId,
            @Parameter(description = "User ID") @PathVariable Long userId) {
        UserBicycleDTO userBicycleDTO = bicycleService.returnBicycle(userId, bicycleId);
        return ResponseEntity.ok(userBicycleDTO);
    }

    @PostMapping("/{bicycleId}/assign/{userId}")
    @Transactional
    @Operation(summary = "Assign user to bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User assigned to bicycle", content = @Content(schema = @Schema(implementation = BicycleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Bicycle or user not found"),
            @ApiResponse(responseCode = "409", description = "Bicycle already has an assigned user or is currently rented")
    })
    public ResponseEntity<BicycleResponseDTO> assignUserToBicycle(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId,
            @Parameter(description = "User ID") @PathVariable Long userId) {
        Bicycle bicycle = bicycleService.assignUserToBicycle(bicycleId, userId);
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


    @GetMapping("/rentals")
    @Operation(summary = "Get all bicycle rental records")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found rental records", content = @Content(schema = @Schema(implementation = UserBicycleDTO.class))),
            @ApiResponse(responseCode = "404", description = "No rental records found")
    })
    public ResponseEntity<List<UserBicycleDTO>> getAllRentals() {
        List<UserBicycleDTO> rentals = bicycleService.getAllRentals();
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/rentals/user/{userId}")
    @Operation(summary = "Get rental records for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found rental records for user", content = @Content(schema = @Schema(implementation = UserBicycleDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found or no rental records for user")
    })
    public ResponseEntity<List<UserBicycleDTO>> getRentalsForUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        List<UserBicycleDTO> rentals = bicycleService.getRentalsForUser(userId);
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/rentals/bicycle/{bicycleId}")
    @Operation(summary = "Get rental records for a specific bicycle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found rental records for bicycle", content = @Content(schema = @Schema(implementation = UserBicycleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Bicycle not found or no rental records for bicycle")
    })
    public ResponseEntity<List<UserBicycleDTO>> getRentalsForBicycle(
            @Parameter(description = "Bicycle ID") @PathVariable Long bicycleId) {
        bicycleService.getBicycleById(bicycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bicycle not found with id " + bicycleId));

        List<UserBicycleDTO> rentals = bicycleService.getRentalsForBicycle(bicycleId);
        return ResponseEntity.ok(rentals);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorDetails> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "Business Rule Violation", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }
}