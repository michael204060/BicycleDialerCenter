package com.bikedc.dao;

import com.bikedc.model.UserBicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBicycleDao extends JpaRepository<UserBicycle, UserBicycle.UserBicycleId> {
}