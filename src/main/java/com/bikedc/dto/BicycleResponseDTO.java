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
    private UserDTO owner;

    public BicycleResponseDTO(Bicycle bicycle) {
        this.id = bicycle.getId();
        this.brand = bicycle.getBrand();
        this.model = bicycle.getModel();
        this.type = bicycle.getType();
        this.price = bicycle.getPrice();
        this.owner = bicycle.getOwner() != null ? new UserDTO(bicycle.getOwner()) : null;
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

    public UserDTO getOwner() {
        return owner;
    }
}

class UserDTO {
    private Long id;
    private String username;
    private String email;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}