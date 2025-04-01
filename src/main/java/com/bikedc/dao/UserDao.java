package com.bikedc.dao;

import com.bikedc.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE (:username IS NULL OR u.username = :username)" +
            " AND (:email IS NULL OR u.email = :email)")
    List<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    // JPQL запрос для обновления велосипедов при удалении пользователя
    @Modifying
    @Transactional
    @Query("UPDATE Bicycle b SET b.owner = NULL WHERE b.owner.id = :userId")
    void unlinkBicyclesFromUser(@Param("userId") Long userId);

    // Native SQL запрос для демонстрации
    @Modifying
    @Transactional
    @Query(value = "UPDATE bicycles SET owner_id = NULL WHERE owner_id = :userId", nativeQuery = true)
    void unlinkBicyclesFromUserNative(@Param("userId") Long userId);
}