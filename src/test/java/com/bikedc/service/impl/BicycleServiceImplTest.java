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
    void getBicyclesByBrandAndModel_ShouldReturnAll_WhenNoFilters() {
        when(bicycleDao.findAll()).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel(null, null);

        assertEquals(1, result.size());
        verify(bicycleDao).findAll();
    }

    @Test
    void getBicycleById_ShouldReturnFromCache() {
        when(bicycleCache.get(1L)).thenReturn(testBicycle);

        Optional<Bicycle> result = bicycleService.getBicycleById(1L);

        assertTrue(result.isPresent());
        verify(bicycleDao, never()).findById(anyLong());
    }

    @Test
    void getBicycleById_ShouldThrow_WhenNotFound() {
        when(bicycleCache.get(1L)).thenReturn(null);
        when(bicycleDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.getBicycleById(1L));
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
    void createBicycles_ShouldHandleEmptyList() {
        List<Bicycle> result = bicycleService.createBicycles(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void updateBicycles_ShouldUpdateAllAndCache() {
        Bicycle bike1 = new Bicycle();
        bike1.setId(1L);
        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);

        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Bicycle> result = bicycleService.updateBicycles(List.of(bike1, bike2));

        assertEquals(2, result.size());
        verify(bicycleCache, times(2)).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void updateBicycles_ShouldHandleEmptyList() {
        List<Bicycle> result = bicycleService.updateBicycles(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void createBicycles_ShouldHandleOwnerAssociation() {
        Bicycle bike = new Bicycle();
        bike.setId(1L);
        bike.setOwner(testUser);

        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));
        when(entityManager.merge(any(User.class))).thenReturn(testUser);

        List<Bicycle> result = bicycleService.createBicycles(List.of(bike));

        assertEquals(1, result.size());
        verify(entityManager).merge(testUser);
    }

    @Test
    void rentBicycle_ShouldCreateRentalRecord() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.save(any(UserBicycle.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBicycle result = bicycleService.rentBicycle(1L, 1L);

        assertNotNull(result.getRentStartTime());
        assertNull(result.getRentEndTime());
    }

    @Test
    void deleteBicycle_ShouldEvictFromCache() {
        when(bicycleDao.existsById(1L)).thenReturn(true);

        bicycleService.deleteBicycle(1L);

        verify(bicycleCache).evict(1L);
        verify(bicycleDao).deleteById(1L);
    }
}