package com.smartpark.dto;

import com.smartpark.entity.ParkingArea;

import java.time.LocalTime;

public class ParkingAreaDto {

    private Long id;
    private String name;
    private String address;
    private String description;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean active;
    private int totalSlots;
    private int availableSlots;

    public ParkingAreaDto() {
    }

    public static ParkingAreaDto fromEntity(ParkingArea area) {
        ParkingAreaDto dto = new ParkingAreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setAddress(area.getAddress());
        dto.setDescription(area.getDescription());
        dto.setOpeningTime(area.getOpeningTime());
        dto.setClosingTime(area.getClosingTime());
        dto.setActive(area.isActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }
}
