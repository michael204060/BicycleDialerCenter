package com.bikedc.service.impl;

import com.bikedc.cache.BicycleCache;
import com.bikedc.dao.BicycleDao;
import com.bikedc.dao.UserBicycleDao;
import com.bikedc.dao.UserDao;
import com.bikedc.exception.ResourceNotFoundException;
import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import com.bikedc.model.UserBicycle;
import com.bikedc.dto.UserBicycleDTO;
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
        verify(bicycleDao, never()).findByBrandContainingIgnoreCase(anyString());
        verify(bicycleDao, never()).findByModelContainingIgnoreCase(anyString());
        verify(bicycleDao, never()).findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void getBicyclesByBrandAndModel_ShouldFilterByBrand() {
        when(bicycleDao.findByBrandContainingIgnoreCase("TestBrand")).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel("TestBrand", null);

        assertEquals(1, result.size());
        verify(bicycleDao).findByBrandContainingIgnoreCase("TestBrand");
        verify(bicycleDao, never()).findAll();
        verify(bicycleDao, never()).findByModelContainingIgnoreCase(anyString());
        verify(bicycleDao, never()).findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void getBicyclesByBrandAndModel_ShouldFilterByModel() {
        when(bicycleDao.findByModelContainingIgnoreCase("TestModel")).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel(null, "TestModel");

        assertEquals(1, result.size());
        verify(bicycleDao).findByModelContainingIgnoreCase("TestModel");
        verify(bicycleDao, never()).findAll();
        verify(bicycleDao, never()).findByBrandContainingIgnoreCase(anyString());
        verify(bicycleDao, never()).findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void getBicyclesByBrandAndModel_ShouldFilterByBrandAndModel() {
        when(bicycleDao.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase("TestBrand", "TestModel"))
                .thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByBrandAndModel("TestBrand", "TestModel");

        assertEquals(1, result.size());
        verify(bicycleDao).findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase("TestBrand", "TestModel");
        verify(bicycleDao, never()).findAll();
        verify(bicycleDao, never()).findByBrandContainingIgnoreCase(anyString());
        verify(bicycleDao, never()).findByModelContainingIgnoreCase(anyString());
    }

    @Test
    void getBicyclesByAssignedUser_ShouldReturnBicycles_WhenUserExists() {
        when(bicycleDao.findByAssignedUserId(1L)).thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByAssignedUser(1L);

        assertEquals(1, result.size());
        assertEquals(testBicycle, result.get(0));
        verify(bicycleDao).findByAssignedUserId(1L);
    }

    @Test
    void getBicyclesByAssignedUser_ShouldThrowWhenNotFound() {
        when(bicycleDao.findByAssignedUserId(1L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.getBicyclesByAssignedUser(1L));
        verify(bicycleDao).findByAssignedUserId(1L);
    }

    @Test
    void getBicyclesByAssignedUserAttributes_ShouldReturnBicycles() {
        when(bicycleDao.findByAssignedUserAttributes(1L, "testUser", "test@example.com"))
                .thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByAssignedUserAttributes(1L, "testUser", "test@example.com");

        assertEquals(1, result.size());
        verify(bicycleDao).findByAssignedUserAttributes(1L, "testUser", "test@example.com");
    }

    @Test
    void getBicyclesByAssignedUserAttributes_ShouldHandleNullParameters() {
        when(bicycleDao.findByAssignedUserAttributes(null, null, null))
                .thenReturn(List.of(testBicycle));

        List<Bicycle> result = bicycleService.getBicyclesByAssignedUserAttributes(null, null, null);

        assertEquals(1, result.size());
        verify(bicycleDao).findByAssignedUserAttributes(null, null, null);
    }

    @Test
    void getBicyclesByAssignedUserAttributes_ShouldThrowWhenNotFound() {
        when(bicycleDao.findByAssignedUserAttributes(1L, "test", "test@test.com")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> bicycleService.getBicyclesByAssignedUserAttributes(1L, "test", "test@test.com"));
        verify(bicycleDao).findByAssignedUserAttributes(1L, "test", "test@test.com");
    }

    @Test
    void getBicycleById_ShouldReturnFromCache() {
        when(bicycleCache.get(1L)).thenReturn(testBicycle);

        Optional<Bicycle> result = bicycleService.getBicycleById(1L);

        assertTrue(result.isPresent());
        assertEquals(testBicycle, result.get());
        verify(bicycleCache).get(1L);
        verify(bicycleDao, never()).findById(anyLong());
    }

    @Test
    void getBicycleById_ShouldGetFromDaoWhenNotInCache() {
        when(bicycleCache.get(1L)).thenReturn(null);
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Optional<Bicycle> result = bicycleService.getBicycleById(1L);

        assertTrue(result.isPresent());
        assertEquals(testBicycle, result.get());
        verify(bicycleCache).get(1L);
        verify(bicycleDao).findById(1L);
        verify(bicycleCache).put(1L, testBicycle);
    }


    @Test
    void getBicycleById_ShouldThrow_WhenNotFound() {
        when(bicycleCache.get(1L)).thenReturn(null);
        when(bicycleDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.getBicycleById(1L));
        verify(bicycleCache).get(1L);
        verify(bicycleDao).findById(1L);
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void createBicycle_ShouldSaveAndCache() {
        when(bicycleDao.save(any(Bicycle.class))).thenReturn(testBicycle);
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Bicycle result = bicycleService.createBicycle(new Bicycle());

        assertNotNull(result);
        verify(bicycleDao).save(any(Bicycle.class));
        verify(bicycleCache).put(1L, testBicycle);
        verify(userDao, never()).findById(anyLong());
    }

    @Test
    void createBicycle_ShouldAssignUserAndSaveAndCache() {
        Bicycle bicycleWithUser = new Bicycle();
        bicycleWithUser.setAssignedUser(testUser);
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.save(any(Bicycle.class))).thenReturn(testBicycle);
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Bicycle result = bicycleService.createBicycle(bicycleWithUser);

        assertNotNull(result);
        verify(userDao).findById(1L);
        verify(bicycleDao).save(any(Bicycle.class));
        verify(bicycleCache).put(1L, testBicycle);
    }

    @Test
    void createBicycle_ShouldThrowWhenAssignedUserNotFound() {
        Bicycle bicycleWithUser = new Bicycle();
        bicycleWithUser.setAssignedUser(testUser);
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.createBicycle(bicycleWithUser));
        verify(userDao).findById(1L);
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void createBicycles_ShouldSaveAllAndCache() {
        Bicycle bike1 = new Bicycle();
        bike1.setId(1L);
        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);

        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        List<Bicycle> result = bicycleService.createBicycles(List.of(bike1, bike2));

        assertEquals(2, result.size());
        verify(bicycleDao, times(2)).save(any(Bicycle.class));
        verify(bicycleCache, times(2)).put(anyLong(), any(Bicycle.class));
        verify(userDao, never()).findAllById(anyList());
    }

    @Test
    void createBicycles_ShouldHandleEmptyList() {
        List<Bicycle> result = bicycleService.createBicycles(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
        verify(userDao, never()).findAllById(anyList());
    }

    @Test
    void createBicycles_ShouldAssignUsersAndSaveAndCache() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        Bicycle bike1 = new Bicycle();
        bike1.setId(1L);
        bike1.setAssignedUser(testUser);

        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);
        bike2.setAssignedUser(user2);

        when(userDao.findAllById(List.of(1L, 2L))).thenReturn(List.of(testUser, user2));
        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        List<Bicycle> result = bicycleService.createBicycles(List.of(bike1, bike2));

        assertEquals(2, result.size());
        verify(userDao).findAllById(List.of(1L, 2L));
        verify(bicycleDao, times(2)).save(any(Bicycle.class));
        verify(bicycleCache, times(2)).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void createBicycles_ShouldThrowWhenAssignedUserNotFoundInBulk() {
        User user2 = new User();
        user2.setId(2L);

        Bicycle bike1 = new Bicycle();
        bike1.setId(1L);
        bike1.setAssignedUser(testUser);

        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);
        bike2.setAssignedUser(user2);

        when(userDao.findAllById(List.of(1L, 2L))).thenReturn(List.of(testUser));

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.createBicycles(List.of(bike1, bike2)));

        verify(userDao).findAllById(List.of(1L, 2L));
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }


    @Test
    void updateBicycle_ShouldUpdateAndCache() {
        Bicycle existingBicycle = new Bicycle();
        existingBicycle.setId(1L);
        existingBicycle.setBrand("Old");
        existingBicycle.setModel("Old");

        Bicycle updatedDetails = new Bicycle();
        updatedDetails.setId(1L);
        updatedDetails.setBrand("New");
        updatedDetails.setModel("New");

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(existingBicycle));
        when(bicycleDao.save(existingBicycle)).thenReturn(existingBicycle);
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Bicycle result = bicycleService.updateBicycle(updatedDetails);

        assertNotNull(result);
        assertEquals("New", result.getBrand());
        assertEquals("New", result.getModel());
        verify(bicycleDao).findById(1L);
        verify(bicycleDao).save(existingBicycle);
        verify(bicycleCache).put(1L, existingBicycle);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void updateBicycle_ShouldHandleNullAssignedUser() {
        Bicycle existingBicycle = new Bicycle();
        existingBicycle.setId(1L);
        existingBicycle.setBrand("Old");
        existingBicycle.setModel("Old");
        existingBicycle.setAssignedUser(testUser);

        Bicycle updatedDetails = new Bicycle();
        updatedDetails.setId(1L);
        updatedDetails.setBrand("New");
        updatedDetails.setModel("New");
        updatedDetails.setAssignedUser(null);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(existingBicycle));
        when(bicycleDao.save(existingBicycle)).thenReturn(existingBicycle);
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Bicycle result = bicycleService.updateBicycle(updatedDetails);

        assertNotNull(result);
        assertNull(result.getAssignedUser());
        verify(bicycleDao).findById(1L);
        verify(bicycleDao).save(existingBicycle);
        verify(bicycleCache).put(1L, existingBicycle);
        verify(userDao, never()).findById(anyLong());
    }

    @Test
    void updateBicycle_ShouldAssignNewUser() {
        Bicycle existingBicycle = new Bicycle();
        existingBicycle.setId(1L);
        existingBicycle.setAssignedUser(null);

        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newUser");

        Bicycle updatedDetails = new Bicycle();
        updatedDetails.setId(1L);
        updatedDetails.setAssignedUser(newUser);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(existingBicycle));
        when(userDao.findById(2L)).thenReturn(Optional.of(newUser));
        when(bicycleDao.save(existingBicycle)).thenReturn(existingBicycle);
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Bicycle result = bicycleService.updateBicycle(updatedDetails);

        assertNotNull(result);
        assertEquals(newUser, result.getAssignedUser());
        verify(bicycleDao).findById(1L);
        verify(userDao).findById(2L);
        verify(bicycleDao).save(existingBicycle);
        verify(bicycleCache).put(1L, existingBicycle);
    }

    @Test
    void updateBicycle_ShouldThrowWhenBicycleNotFound() {
        Bicycle updatedDetails = new Bicycle();
        updatedDetails.setId(1L);

        when(bicycleDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.updateBicycle(updatedDetails));
        verify(bicycleDao).findById(1L);
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
        verify(userDao, never()).findById(anyLong());
    }

    @Test
    void updateBicycle_ShouldThrowWhenNewAssignedUserNotFound() {
        Bicycle existingBicycle = new Bicycle();
        existingBicycle.setId(1L);

        User nonExistentUser = new User();
        nonExistentUser.setId(99L);

        Bicycle updatedDetails = new Bicycle();
        updatedDetails.setId(1L);
        updatedDetails.setAssignedUser(nonExistentUser);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(existingBicycle));
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.updateBicycle(updatedDetails));
        verify(bicycleDao).findById(1L);
        verify(userDao).findById(99L);
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }


    @Test
    void updateBicycles_ShouldUpdateAllAndCache() {
        Bicycle existingBike1 = new Bicycle();
        existingBike1.setId(1L);
        existingBike1.setBrand("Old1");
        Bicycle existingBike2 = new Bicycle();
        existingBike2.setId(2L);
        existingBike2.setBrand("Old2");

        Bicycle updatedDetails1 = new Bicycle();
        updatedDetails1.setId(1L);
        updatedDetails1.setBrand("New1");
        Bicycle updatedDetails2 = new Bicycle();
        updatedDetails2.setId(2L);
        updatedDetails2.setBrand("New2");

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(existingBike1));
        when(bicycleDao.findById(2L)).thenReturn(Optional.of(existingBike2));
        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        List<Bicycle> result = bicycleService.updateBicycles(List.of(updatedDetails1, updatedDetails2));

        assertEquals(2, result.size());
        assertEquals("New1", result.get(0).getBrand());
        assertEquals("New2", result.get(1).getBrand());
        verify(bicycleDao, times(2)).findById(anyLong());
        verify(bicycleDao, times(2)).save(any(Bicycle.class));
        verify(bicycleCache, times(2)).put(anyLong(), any(Bicycle.class));
        verify(userDao, never()).findAllById(anyList());
    }

    @Test
    void updateBicycles_ShouldHandleEmptyList() {
        List<Bicycle> result = bicycleService.updateBicycles(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(bicycleDao, never()).findById(anyLong());
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
        verify(userDao, never()).findAllById(anyList());
    }

    @Test
    void updateBicycles_ShouldAssignUsersInBulk() {
        User user2 = new User();
        user2.setId(2L);

        Bicycle existingBike1 = new Bicycle();
        existingBike1.setId(1L);
        existingBike1.setAssignedUser(null);

        Bicycle existingBike2 = new Bicycle();
        existingBike2.setId(2L);
        existingBike2.setAssignedUser(testUser);

        Bicycle updatedDetails1 = new Bicycle();
        updatedDetails1.setId(1L);
        updatedDetails1.setAssignedUser(testUser);

        Bicycle updatedDetails2 = new Bicycle();
        updatedDetails2.setId(2L);
        updatedDetails2.setAssignedUser(user2);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(existingBike1));
        when(bicycleDao.findById(2L)).thenReturn(Optional.of(existingBike2));
        when(userDao.findAllById(List.of(1L, 2L))).thenReturn(List.of(testUser, user2));
        when(bicycleDao.save(any(Bicycle.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        List<Bicycle> result = bicycleService.updateBicycles(List.of(updatedDetails1, updatedDetails2));

        assertEquals(2, result.size());
        assertEquals(testUser, result.get(0).getAssignedUser());
        assertEquals(user2, result.get(1).getAssignedUser());

        verify(bicycleDao, times(2)).findById(anyLong());
        verify(userDao).findAllById(List.of(1L, 2L));
        verify(bicycleDao, times(2)).save(any(Bicycle.class));
        verify(bicycleCache, times(2)).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void rentBicycle_ShouldCreateRentalRecord() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.findByUserIdAndBicycleId(1L, 1L)).thenReturn(Optional.empty());
        when(userBicycleDao.save(any(UserBicycle.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBicycleDTO result = bicycleService.rentBicycle(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getRentStartTime());
        assertNull(result.getRentEndTime());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testBicycle.getBrand(), result.getBicycleBrand());
        assertEquals(testBicycle.getModel(), result.getBicycleModel());
        verify(userDao).findById(1L);
        verify(bicycleDao).findById(1L);
        verify(userBicycleDao).findByUserIdAndBicycleId(1L, 1L);
        verify(userBicycleDao).save(any(UserBicycle.class));
    }

    @Test
    void rentBicycle_ShouldThrowWhenUserNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.rentBicycle(1L, 1L));
        verify(userDao).findById(1L);
        verify(bicycleDao, never()).findById(anyLong());
        verify(userBicycleDao, never()).save(any(UserBicycle.class));
    }

    @Test
    void rentBicycle_ShouldThrowWhenBicycleNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.rentBicycle(1L, 1L));
        verify(userDao).findById(1L);
        verify(bicycleDao).findById(1L);
        verify(userBicycleDao, never()).save(any(UserBicycle.class));
    }

    @Test
    void rentBicycle_ShouldThrowWhenAlreadyCurrentlyRentedByUser() {
        UserBicycle activeRental = new UserBicycle(testUser, testBicycle);
        activeRental.setRentStartTime(LocalDateTime.now());
        activeRental.setRentEndTime(null);

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.findByUserIdAndBicycleId(1L, 1L)).thenReturn(Optional.of(activeRental));

        assertThrows(IllegalStateException.class, () -> bicycleService.rentBicycle(1L, 1L));
        verify(userDao).findById(1L);
        verify(bicycleDao).findById(1L);
        verify(userBicycleDao).findByUserIdAndBicycleId(1L, 1L);
        verify(userBicycleDao, never()).save(any(UserBicycle.class));
    }


    @Test
    void returnBicycle_ShouldSetEndTime() {
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(1L, 1L);
        UserBicycle rental = new UserBicycle(testUser, testBicycle);
        rental.setRentStartTime(LocalDateTime.now().minusHours(1));
        rental.setRentEndTime(null);

        when(userBicycleDao.findById(id)).thenReturn(Optional.of(rental));
        when(userBicycleDao.save(rental)).thenReturn(rental);

        UserBicycleDTO result = bicycleService.returnBicycle(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getRentEndTime());
        verify(userBicycleDao).findById(id);
        verify(userBicycleDao).save(rental);
    }

    @Test
    void returnBicycle_ShouldThrowWhenNotFound() {
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(1L, 1L);
        when(userBicycleDao.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.returnBicycle(1L, 1L));
        verify(userBicycleDao).findById(id);
        verify(userBicycleDao, never()).save(any(UserBicycle.class));
    }

    @Test
    void returnBicycle_ShouldThrowWhenAlreadyReturned() {
        UserBicycle.UserBicycleId id = new UserBicycle.UserBicycleId(1L, 1L);
        UserBicycle rental = new UserBicycle(testUser, testBicycle);
        rental.setRentStartTime(LocalDateTime.now().minusHours(2));
        rental.setRentEndTime(LocalDateTime.now().minusHours(1));

        when(userBicycleDao.findById(id)).thenReturn(Optional.of(rental));

        assertThrows(IllegalStateException.class, () -> bicycleService.returnBicycle(1L, 1L));
        verify(userBicycleDao).findById(id);
        verify(userBicycleDao, never()).save(any(UserBicycle.class));
    }


    @Test
    void deleteBicycle_ShouldEvictFromCache() {
        when(bicycleDao.existsById(1L)).thenReturn(true);
        doNothing().when(bicycleDao).deleteById(1L);
        doNothing().when(bicycleCache).evict(anyLong());

        bicycleService.deleteBicycle(1L);

        verify(bicycleCache).evict(1L);
        verify(bicycleDao).existsById(1L);
        verify(bicycleDao).deleteById(1L);
    }

    @Test
    void deleteBicycle_ShouldThrow_WhenBicycleNotFound() {
        when(bicycleDao.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.deleteBicycle(1L));
        verify(bicycleDao).existsById(1L);
        verify(bicycleCache, never()).evict(anyLong());
        verify(bicycleDao, never()).deleteById(anyLong());
    }

    @Test
    void assignUserToBicycle_ShouldAssignUser_WhenNoUser() {
        testBicycle.setAssignedUser(null);
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBicycleDao.findByBicycleId(1L)).thenReturn(Collections.emptyList());
        when(bicycleDao.save(testBicycle)).thenReturn(testBicycle);
        doNothing().when(bicycleCache).put(anyLong(), any(Bicycle.class));

        Bicycle result = bicycleService.assignUserToBicycle(1L, 1L);

        assertNotNull(result);
        assertEquals(testUser, result.getAssignedUser());
        verify(bicycleDao).findById(1L);
        verify(userDao).findById(1L);
        verify(userBicycleDao).findByBicycleId(1L);
        verify(bicycleDao).save(testBicycle);
        verify(bicycleCache).put(1L, testBicycle);
    }

    @Test
    void assignUserToBicycle_ShouldThrowWhenBicycleNotFound() {
        when(bicycleDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.assignUserToBicycle(1L, 1L));
        verify(bicycleDao).findById(1L);
        verify(userDao, never()).findById(anyLong());
        verify(userBicycleDao, never()).findByBicycleId(anyLong());
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void assignUserToBicycle_ShouldThrowWhenUserNotFound() {
        testBicycle.setAssignedUser(null);
        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        when(userBicycleDao.findByBicycleId(1L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> bicycleService.assignUserToBicycle(1L, 1L));
        verify(bicycleDao).findById(1L);
        verify(userDao).findById(1L);
        verify(userBicycleDao).findByBicycleId(1L);
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void assignUserToBicycle_ShouldThrowWhenAlreadyAssignedToDifferentUser() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        testBicycle.setAssignedUser(anotherUser);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.findByBicycleId(1L)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> bicycleService.assignUserToBicycle(1L, 1L));
        verify(bicycleDao).findById(1L);
        verify(userBicycleDao).findByBicycleId(1L);
        verify(userDao, never()).findById(anyLong());
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void assignUserToBicycle_ShouldReturnSameBicycleWhenAlreadyAssignedToSameUser() {
        testBicycle.setAssignedUser(testUser);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.findByBicycleId(1L)).thenReturn(Collections.emptyList());

        Bicycle result = bicycleService.assignUserToBicycle(1L, 1L);

        assertNotNull(result);
        assertEquals(testUser, result.getAssignedUser());
        verify(bicycleDao).findById(1L);
        verify(userBicycleDao).findByBicycleId(1L);
        verify(userDao, never()).findById(anyLong());
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }


    @Test
    void assignUserToBicycle_ShouldThrowWhenBicycleCurrentlyRented() {
        testBicycle.setAssignedUser(null);
        UserBicycle activeRental = new UserBicycle(testUser, testBicycle);
        activeRental.setRentStartTime(LocalDateTime.now());
        activeRental.setRentEndTime(null);

        when(bicycleDao.findById(1L)).thenReturn(Optional.of(testBicycle));
        when(userBicycleDao.findByBicycleId(1L)).thenReturn(List.of(activeRental));

        assertThrows(IllegalStateException.class, () -> bicycleService.assignUserToBicycle(1L, 2L));
        verify(bicycleDao).findById(1L);
        verify(userBicycleDao).findByBicycleId(1L);
        verify(userDao, never()).findById(anyLong());
        verify(bicycleDao, never()).save(any(Bicycle.class));
        verify(bicycleCache, never()).put(anyLong(), any(Bicycle.class));
    }

    @Test
    void getAllRentals_ShouldReturnListOfRentals() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);
        bike2.setBrand("Brand2");
        bike2.setModel("Model2");

        UserBicycle rental1 = new UserBicycle(testUser, testBicycle);
        rental1.setRentStartTime(LocalDateTime.now());

        UserBicycle rental2 = new UserBicycle(user2, bike2);
        rental2.setRentStartTime(LocalDateTime.now().minusDays(1));
        rental2.setRentEndTime(LocalDateTime.now().minusHours(1));

        List<UserBicycle> rentals = List.of(rental1, rental2);

        when(userBicycleDao.findAll()).thenReturn(rentals);

        List<UserBicycleDTO> result = bicycleService.getAllRentals();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(rental1.getUser().getUsername(), result.get(0).getUsername());
        assertEquals(rental1.getBicycle().getBrand(), result.get(0).getBicycleBrand());
        assertNotNull(result.get(0).getRentStartTime());
        assertNull(result.get(0).getRentEndTime());

        assertEquals(rental2.getUser().getUsername(), result.get(1).getUsername());
        assertEquals(rental2.getBicycle().getBrand(), result.get(1).getBicycleBrand());
        assertNotNull(result.get(1).getRentStartTime());
        assertNotNull(result.get(1).getRentEndTime());

        verify(userBicycleDao).findAll();
    }

    @Test
    void getAllRentals_ShouldReturnEmptyListWhenNoneExist() {
        when(userBicycleDao.findAll()).thenReturn(Collections.emptyList());

        List<UserBicycleDTO> result = bicycleService.getAllRentals();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userBicycleDao).findAll();
    }

    @Test
    void getRentalsForUser_ShouldReturnRentalsForSpecificUser() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);
        bike2.setBrand("Brand2");

        UserBicycle rental1 = new UserBicycle(testUser, testBicycle);
        UserBicycle rental2 = new UserBicycle(user2, bike2);

        when(userBicycleDao.findByUserId(1L)).thenReturn(List.of(rental1));

        List<UserBicycleDTO> result = bicycleService.getRentalsForUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userBicycleDao).findByUserId(1L);
    }

    @Test
    void getRentalsForUser_ShouldReturnEmptyListWhenUserHasNoRentals() {
        when(userBicycleDao.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<UserBicycleDTO> result = bicycleService.getRentalsForUser(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userBicycleDao).findByUserId(1L);
    }

    @Test
    void getRentalsForBicycle_ShouldReturnRentalsForSpecificBicycle() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        Bicycle bike2 = new Bicycle();
        bike2.setId(2L);
        bike2.setBrand("Brand2");

        UserBicycle rental1 = new UserBicycle(testUser, testBicycle);
        UserBicycle rental2 = new UserBicycle(user2, bike2);
        UserBicycle rental3 = new UserBicycle(user2, testBicycle);

        when(userBicycleDao.findByBicycleId(1L)).thenReturn(List.of(rental1, rental3));

        List<UserBicycleDTO> result = bicycleService.getRentalsForBicycle(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testBicycle.getModel(), result.get(0).getBicycleModel());
        assertEquals(testBicycle.getModel(), result.get(1).getBicycleModel());
        verify(userBicycleDao).findByBicycleId(1L);
    }

    @Test
    void getRentalsForBicycle_ShouldReturnEmptyListWhenBicycleHasNoRentals() {
        when(userBicycleDao.findByBicycleId(1L)).thenReturn(Collections.emptyList());

        List<UserBicycleDTO> result = bicycleService.getRentalsForBicycle(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userBicycleDao).findByBicycleId(1L);
    }
}