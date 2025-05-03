package com.bikedc.dao;

import com.bikedc.model.Bicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BicycleDao extends JpaRepository<Bicycle, Long> {
    @Query("SELECT b FROM Bicycle b WHERE b.assignedUser.id = :userId")
    List<Bicycle> findByAssignedUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM bicycles WHERE owner_id = :userId", nativeQuery = true)
    List<Bicycle> findByAssignedUserIdNative(@Param("userId") Long userId);

    @Query("SELECT b FROM Bicycle b WHERE " +
            "(:userId IS NULL OR b.assignedUser.id = :userId) " +
            "AND (:username IS NULL OR b.assignedUser.username LIKE %:username%) " +
            "AND (:email IS NULL OR b.assignedUser.email LIKE %:email%)")
    List<Bicycle> findByAssignedUserAttributes(@Param("userId") Long userId,
                                               @Param("username") String username,
                                               @Param("email") String email);

    List<Bicycle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model);
    List<Bicycle> findByBrandContainingIgnoreCase(String brand);
    List<Bicycle> findByModelContainingIgnoreCase(String model);
    List<Bicycle> findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(String brand, String model);
}