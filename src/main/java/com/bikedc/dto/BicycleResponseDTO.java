package com.bikedc.dto;

import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import java.math.BigDecimal;

public class BicycleResponseDTO {
    private Long id;
    private String brand;
    private String model;
    private String type;
    private BigDecimal price;
    private UserDTO assignedUser;

    public BicycleResponseDTO(Bicycle bicycle) {
        this.id = bicycle.getId();
        this.brand = bicycle.getBrand();
        this.model = bicycle.getModel();
        this.type = bicycle.getType();
        this.price = bicycle.getPrice();
        User user = bicycle.getAssignedUser();
        this.assignedUser = user != null ? new UserDTO(user) : null;
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public UserDTO getAssignedUser() {
        return assignedUser;
    }
}