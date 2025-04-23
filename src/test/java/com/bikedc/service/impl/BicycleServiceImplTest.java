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
    void getBicyclesByBrandAndModel_ShouldFilterByBrand() {
        when(bicycleDao.findByBrandContainingIgnoreCase("TestBrand")).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel("TestBrand", null);

        assertEquals(1, result.size());
        verify(bicycleDao).findByBrandContainingIgnoreCase("TestBrand");
    }

    @Test
    void matchBicycleWithOwner_ShouldThrowWhenBicycleAlreadyHasOwner() {
        
        Bicycle ownedBicycle = new Bicycle();
        ownedBicycle.setId(1L);
        ownedBicycle.setOwner(testUser); 

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(ownedBicycle));

        
        assertThrows(IllegalStateException.class,
                () -> bicycleService.matchBicycleWithOwner(1L, 2L));

        
        verify(bicycleDao, never()).save(any());
        verify(bicycleCache, never()).put(anyLong(), any());
    }

    @Test
    void getBicycleById_ShouldGetFromDaoWhenNotInCache() {
        
        when(bicycleCache.get(1L)).thenReturn(null);
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));

        
        Optional<Bicycle> result = bicycleService.getBicycleById(1L);

        
        assertTrue(result.isPresent());
        assertEquals(testBicycle, result.get());
        verify(bicycleDao).findById(1L);
    }

    @Test
    void updateBicycle_ShouldHandleNullOwner() {
        
        Bicycle bicycle = new Bicycle();
        bicycle.setId(1L);
        bicycle.setBrand("Test");
        bicycle.setModel("Model");

        when(bicycleDao.save(any(Bicycle.class))).thenReturn(bicycle);

        
        Bicycle result = bicycleService.updateBicycle(bicycle);

        
        assertNotNull(result);
        assertNull(result.getOwner());
        verify(bicycleCache).put(1L, bicycle);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void getBicyclesByOwnerAttributes_ShouldHandleNullParameters() {
        
        when(bicycleDao.findByOwnerAttributes(null, null, null))
                .thenReturn(List.of(testBicycle));

        
        List<Bicycle> result = bicycleService.getBicyclesByOwnerAttributes(null, null, null);

        
        assertEquals(1, result.size());
        verify(bicycleDao).findByOwnerAttributes(null, null, null);
    }
    
    @Test
    void getBicyclesByBrandAndModel_ShouldFilterByModel() {
        when(bicycleDao.findByModelContainingIgnoreCase("TestModel")).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel(null, "TestModel");

        assertEquals(1, result.size());
        verify(bicycleDao).findByModelContainingIgnoreCase("TestModel");
    }

    @Test
    void getBicyclesByBrandAndModel_ShouldFilterByBrandAndModel() {
        when(bicycleDao.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase("TestBrand", "TestModel"))
                .thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel("TestBrand", "TestModel");

        assertEquals(1, result.size());
        verify(bicycleDao).findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase("TestBrand", "TestModel");
    }

    @Test
    void getBicyclesByOwner_ShouldThrowWhenNotFound() {
        when(bicycleDao.findByOwnerId(1L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.getBicyclesByOwner(1L));
    }

    @Test
    void getBicyclesByOwnerAttributes_ShouldThrowWhenNotFound() {
        when(bicycleDao.findByOwnerAttributes(1L, "test", "test@test.com")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> bicycleService.getBicyclesByOwnerAttributes(1L, "test", "test@test.com"));
    }

    @Test
    void returnBicycle_ShouldSetEndTime() {
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(1L, 1L);
        UserBicycle rental = new UserBicycle(testUser, testBicycle);
        when(userBicycleDao.findById(id)).thenReturn(Optional.of(rental));
        when(userBicycleDao.save(rental)).thenReturn(rental);

        UserBicycle result = bicycleService.returnBicycle(1L, 1L);

        assertNotNull(result.getRentEndTime());
    }

    @Test
    void returnBicycle_ShouldThrowWhenNotFound() {
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(1L, 1L);
        when(userBicycleDao.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.returnBicycle(1L, 1L));
    }

    @Test
    void matchBicycleWithOwner_ShouldSetOwner() {
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.save(testBicycle)).thenReturn(testBicycle);

        Bicycle result = bicycleService.matchBicycleWithOwner(1L, 1L);

        assertEquals(testUser, result.getOwner());
        verify(bicycleCache).put(1L, testBicycle);
    }

    @Test
    void matchBicycleWithOwner_ShouldThrowWhenBicycleNotFound() {
        when(bicycleDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.matchBicycleWithOwner(1L, 1L));
    }

    @Test
    void matchBicycleWithOwner_ShouldThrowWhenUserNotFound() {
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.matchBicycleWithOwner(1L, 1L));
    }

    @Test
    void matchBicycleWithOwner_ShouldThrowWhenAlreadyOwned() {
        testBicycle.setOwner(testUser);
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));

        assertThrows(IllegalStateException.class, () -> bicycleService.matchBicycleWithOwner(1L, 1L));
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
    @Test
    void deleteBicycle_ShouldThrow_WhenBicycleNotFound() {
        when(bicycleDao.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.deleteBicycle(1L));
        verify(bicycleCache, never()).evict(anyLong());
        verify(bicycleDao, never()).deleteById(anyLong());
    }
    @Test
    void getBicyclesByOwner_ShouldReturnBicycles_WhenOwnerExists() {
        when(bicycleDao.findByOwnerId(1L)).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByOwner(1L);

        assertEquals(1, result.size());
        assertEquals(testBicycle, result.get(0));
    }
}