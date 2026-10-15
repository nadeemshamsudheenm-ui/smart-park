package com.smartpark.dto;

import com.smartpark.entity.Vehicle;
import com.smartpark.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VehicleDto {

    private Long id;
    private Long userId;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private LocalDateTime createdAt;

    public VehicleDto() {
    }

    public static VehicleDto fromEntity(Vehicle vehicle) {
        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        if (vehicle.getUser() != null) {
            dto.setUserId(vehicle.getUser().getId());
        }
        dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setCreatedAt(vehicle.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
