package com.bikedc.dao;

import com.bikedc.model.Bicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BicycleDao extends JpaRepository<Bicycle, Long> {
    @Query("SELECT b FROM Bicycle b WHERE b.owner.id = :ownerId")
    List<Bicycle> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = "SELECT * FROM bicycles WHERE owner_id = :ownerId", nativeQuery = true)
    List<Bicycle> findByOwnerIdNative(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Bicycle b WHERE " +
            "(:ownerId IS NULL OR b.owner.id = :ownerId) " +
            "AND (:ownerName IS NULL OR b.owner.username LIKE %:ownerName%) " +
            "AND (:ownerEmail IS NULL OR b.owner.email LIKE %:ownerEmail%)")
    List<Bicycle> findByOwnerAttributes(@Param("ownerId") Long ownerId,
                                        @Param("ownerName") String ownerName,
                                        @Param("ownerEmail") String ownerEmail);

    List<Bicycle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model);
    List<Bicycle> findByBrandContainingIgnoreCase(String brand);
    List<Bicycle> findByModelContainingIgnoreCase(String model);
    List<Bicycle> findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(String brand, String model);
}