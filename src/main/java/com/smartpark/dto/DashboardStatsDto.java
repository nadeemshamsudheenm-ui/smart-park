package com.smartpark.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {

    private long totalParkingAreas;
    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long reservedSlots;
    private long inactiveSlots;
    private long upcomingReservations;
    private long activeParkingSessions;
    private long totalCompletedBookings;
    private BigDecimal totalRevenue;

    public DashboardStatsDto() {
        this.totalRevenue = BigDecimal.ZERO;
    }

    public long getTotalParkingAreas() {
        return totalParkingAreas;
    }

    public void setTotalParkingAreas(long totalParkingAreas) {
        this.totalParkingAreas = totalParkingAreas;
    }

    public long getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(long totalSlots) {
        this.totalSlots = totalSlots;
    }

    public long getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(long availableSlots) {
        this.availableSlots = availableSlots;
    }

    public long getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(long occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public long getReservedSlots() {
        return reservedSlots;
    }

    public void setReservedSlots(long reservedSlots) {
        this.reservedSlots = reservedSlots;
    }

    public long getInactiveSlots() {
        return inactiveSlots;
    }

    public void setInactiveSlots(long inactiveSlots) {
        this.inactiveSlots = inactiveSlots;
    }

    public long getUpcomingReservations() {
        return upcomingReservations;
    }

    public void setUpcomingReservations(long upcomingReservations) {
        this.upcomingReservations = upcomingReservations;
    }

    public long getActiveParkingSessions() {
        return activeParkingSessions;
    }

    public void setActiveParkingSessions(long activeParkingSessions) {
        this.activeParkingSessions = activeParkingSessions;
    }

    public long getTotalCompletedBookings() {
        return totalCompletedBookings;
    }

    public void setTotalCompletedBookings(long totalCompletedBookings) {
        this.totalCompletedBookings = totalCompletedBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
