package com.bikedc.dao;

import com.bikedc.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE (:username IS NULL OR u.username = :username)" +
            " AND (:email IS NULL OR u.email = :email)")
    List<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
}