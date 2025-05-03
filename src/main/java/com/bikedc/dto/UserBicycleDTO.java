package com.bikedc.dto;

import com.bikedc.model.UserBicycle;
import com.bikedc.model.User;
import com.bikedc.model.Bicycle;
import java.time.LocalDateTime;

public class UserBicycleDTO {
    private Long userId;
    private Long bicycleId;

    private String username;
    private String bicycleBrand;
    private String bicycleModel;

    private LocalDateTime rentStartTime;
    private LocalDateTime rentEndTime;

    public UserBicycleDTO() {}

    public UserBicycleDTO(UserBicycle userBicycle) {
        if (userBicycle != null) {
            User user = userBicycle.getUser();
            Bicycle bicycle = userBicycle.getBicycle();

            this.userId = user != null ? user.getId() : null;
            this.bicycleId = bicycle != null ? bicycle.getId() : null;

            this.username = user != null ? user.getUsername() : "Неизвестный пользователь";
            this.bicycleBrand = bicycle != null ? bicycle.getBrand() : "Неизвестный бренд";
            this.bicycleModel = bicycle != null ? bicycle.getModel() : "Неизвестная модель";

            this.rentStartTime = userBicycle.getRentStartTime();
            this.rentEndTime = userBicycle.getRentEndTime();
        }
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBicycleId() {
        return bicycleId;
    }

    public String getUsername() {
        return username;
    }

    public String getBicycleBrand() {
        return bicycleBrand;
    }

    public String getBicycleModel() {
        return bicycleModel;
    }

    public LocalDateTime getRentStartTime() {
        return rentStartTime;
    }

    public LocalDateTime getRentEndTime() {
        return rentEndTime;
    }
}