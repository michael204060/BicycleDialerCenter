package com.bikedc.dto;

import com.bikedc.model.Bicycle;
import com.bikedc.model.User;
import java.math.BigDecimal;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class BicycleDTO {
    private Long id;
    private String brand;
    private String model;
    private String type;
    private BigDecimal price;
    private Long ownerId;

    public BicycleDTO() {}

    public BicycleDTO(Bicycle bicycle) {
        this.id = bicycle.getId();
        this.brand = bicycle.getBrand();
        this.model = bicycle.getModel();
        this.type = bicycle.getType();
        this.price = bicycle.getPrice();
        User owner = bicycle.getOwner();
        if (owner != null) {
            this.ownerId = owner.getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}