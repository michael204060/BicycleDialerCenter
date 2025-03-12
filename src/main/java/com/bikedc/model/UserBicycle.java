package com.bikedc.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_bicycle")
public class UserBicycle implements Serializable {
    @EmbeddedId
    private UserBicycleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bicycleId")
    @JoinColumn(name = "bicycle_id")
    private Bicycle bicycle;

    private LocalDateTime rentStartTime;

    private LocalDateTime rentEndTime;

    public UserBicycle() {}

    public UserBicycle(User user, Bicycle bicycle) {
        this.user = user;
        this.bicycle = bicycle;
        this.id = new UserBicycleId(user.getId(), bicycle.getId());
    }

    public LocalDateTime getRentStartTime() {
        return rentStartTime;
    }

    public void setRentStartTime(LocalDateTime rentStartTime) {
        this.rentStartTime = rentStartTime;
    }

    public LocalDateTime getRentEndTime() {
        return rentEndTime;
    }

    public void setRentEndTime(LocalDateTime rentEndTime) {
        this.rentEndTime = rentEndTime;
    }

    public UserBicycleId getId() {
        return id;
    }

    public void setId(UserBicycleId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Bicycle getBicycle() {
        return bicycle;
    }

    public void setBicycle(Bicycle bicycle) {
        this.bicycle = bicycle;
    }

    @Embeddable
    public static class UserBicycleId implements Serializable {
        private Long userId;
        private Long bicycleId;

        public UserBicycleId() {}

        public UserBicycleId(Long userId, Long bicycleId) {
            this.userId = userId;
            this.bicycleId = bicycleId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getBicycleId() {
            return bicycleId;
        }

        public void setBicycleId(Long bicycleId) {
            this.bicycleId = bicycleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserBicycleId that = (UserBicycleId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(bicycleId, that.bicycleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, bicycleId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserBicycle that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}