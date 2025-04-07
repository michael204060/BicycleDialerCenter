package com.bikedc.service.impl;

import com.bikedc.cache.BicycleCache;
import com.bikedc.dao.BicycleDao;
import com.bikedc.dao.UserBicycleDao;
import com.bikedc.dao.UserDao;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BicycleServiceImplTest {

    @Mock private BicycleDao bicycleDao;
    @Mock private UserBicycleDao userBicycleDao;
    @Mock private UserDao userDao;
    @Mock private BicycleCache bicycleCache;
    @Mock private EntityManager entityManager;

    @InjectMocks private BicycleServiceImpl bicycleService;

    private Bicycle testBicycle;
    private User testUser;

    @BeforeEach
    void setUp() {
        testBicycle = new Bicycle();
        testBicycle.setId(1L);
        testBicycle.setBrand("TestBrand");
        testBicycle.setModel("TestModel");
        testBicycle.setPrice(BigDecimal.valueOf(1000));

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
    }

    @Test
    void getBicyclesByBrandAndModel_ShouldReturnEmptyList_WhenNoParams() {
        when(bicycleDao.findAll()).thenReturn(Collections.emptyList());

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel(null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBicyclesByBrandAndModel_ShouldReturnFilteredByBrand() {
        when(bicycleDao.findByBrandContainingIgnoreCase("Test")).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel("Test", null);

        assertEquals(1, result.size());
        assertEquals("TestBrand", result.get(0).getBrand());
    }

    @Test
    void getBicycleById_ShouldReturnFromCache() {
        when(bicycleCache.get(1L)).thenReturn(testBicycle);

        Optional<Bicycle> result = bicycleService.getBicycleById(1L);

        assertTrue(result.isPresent());
        verify(bicycleDao, never()).findById(anyLong());
    }

    @Test
    void createBicycle_ShouldSaveAndCache() {
        when(bicycleDao.save(any(Bicycle.class))).thenReturn(testBicycle);

        Bicycle result = bicycleService.createBicycle(testBicycle);

        assertNotNull(result);
        verify(bicycleCache).put(1L, testBicycle);
    }

    @Test
    void createBicycles_ShouldSaveAllAndCache() {
        Bicycle bike1 = new Bicycle();
        bike1.setId(1L);
        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);

        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Bicycle> result = bicycleService.createBicycles(List.of(bike1, bike2));

        assertEquals(2, result.size());
        verify(bicycleCache, times(2)).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void rentBicycle_ShouldThrow_WhenUserNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bicycleService.rentBicycle(1L, 1L));
    }

    @Test
    void rentBicycle_ShouldCreateRentalRecord() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.save(any(UserBicycle.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBicycle result = bicycleService.rentBicycle(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getRentStartTime());
        assertNull(result.getRentEndTime());
    }

    @Test
    void returnBicycle_ShouldUpdateRentalRecord() {
        UserBicycle rental = new UserBicycle(testUser, testBicycle);
        rental.setRentStartTime(LocalDateTime.now().minusDays(1));

        when(userBicycleDao.findById(any())).thenReturn(Optional.of(rental));
        when(userBicycleDao.save(any(UserBicycle.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBicycle result = bicycleService.returnBicycle(1L, 1L);

        assertNotNull(result.getRentEndTime());
    }
}