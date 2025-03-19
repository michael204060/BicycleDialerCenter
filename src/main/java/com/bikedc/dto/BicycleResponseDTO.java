package com.bikedc.dto;

import com.bikedc.model.Bicycle;
import java.math.BigDecimal;

public class BicycleResponseDTO {
    private Long id;
    private String brand;
    private String model;
    private String type;
    private BigDecimal price;
    private Long ownerId;

    public BicycleResponseDTO(Bicycle bicycle) {
        this.id = bicycle.getId();
        this.brand = bicycle.getBrand();
        this.model = bicycle.getModel();
        this.type = bicycle.getType();
        this.price = bicycle.getPrice();
        if (bicycle.getOwner() != null) {
            this.ownerId = bicycle.getOwner().getId();
        }
    }

    public Long getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public Long getOwnerId() { return ownerId; }
}