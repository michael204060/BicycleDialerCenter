package com.bikedc.dao;

import com.bikedc.model.UserBicycle;
import com.bikedc.model.UserBicycle.UserBicycleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBicycleDao extends JpaRepository<UserBicycle, UserBicycleId> {
    List<UserBicycle> findByUserId(Long userId);
    List<UserBicycle> findByBicycleId(Long bicycleId);
    Optional<UserBicycle> findByUserIdAndBicycleId(Long userId, Long bicycleId);
}