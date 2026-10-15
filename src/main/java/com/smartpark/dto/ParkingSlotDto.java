package com.smartpark.dto;

import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.VehicleType;

import java.math.BigDecimal;

public class ParkingSlotDto {

    private Long id;
    private Long parkingAreaId;
    private String parkingAreaName;
    private String slotNumber;
    private VehicleType vehicleType;
    private BigDecimal hourlyRate;
    private SlotStatus status;
    private boolean currentlyAvailable;

    public ParkingSlotDto() {
    }

    public static ParkingSlotDto fromEntity(ParkingSlot slot) {
        ParkingSlotDto dto = new ParkingSlotDto();
        dto.setId(slot.getId());
        if (slot.getParkingArea() != null) {
            dto.setParkingAreaId(slot.getParkingArea().getId());
            dto.setParkingAreaName(slot.getParkingArea().getName());
        }
        dto.setSlotNumber(slot.getSlotNumber());
        dto.setVehicleType(slot.getVehicleType());
        dto.setHourlyRate(slot.getHourlyRate());
        dto.setStatus(slot.getStatus());
        dto.setCurrentlyAvailable(slot.getStatus() == SlotStatus.AVAILABLE);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(Long parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public String getParkingAreaName() {
        return parkingAreaName;
    }

    public void setParkingAreaName(String parkingAreaName) {
        this.parkingAreaName = parkingAreaName;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public boolean isCurrentlyAvailable() {
        return currentlyAvailable;
    }

    public void setCurrentlyAvailable(boolean currentlyAvailable) {
        this.currentlyAvailable = currentlyAvailable;
    }
}
