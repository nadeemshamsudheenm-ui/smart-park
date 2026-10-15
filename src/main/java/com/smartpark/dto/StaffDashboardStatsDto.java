package com.smartpark.dto;

public class StaffDashboardStatsDto {

    private long todayReservations;
    private long expectedArrivals;
    private long currentlyParked;
    private long completedSessions;
    private int assignedAreasCount;

    public StaffDashboardStatsDto() {
    }

    public long getTodayReservations() {
        return todayReservations;
    }

    public void setTodayReservations(long todayReservations) {
        this.todayReservations = todayReservations;
    }

    public long getExpectedArrivals() {
        return expectedArrivals;
    }

    public void setExpectedArrivals(long expectedArrivals) {
        this.expectedArrivals = expectedArrivals;
    }

    public long getCurrentlyParked() {
        return currentlyParked;
    }

    public void setCurrentlyParked(long currentlyParked) {
        this.currentlyParked = currentlyParked;
    }

    public long getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(long completedSessions) {
        this.completedSessions = completedSessions;
    }

    public int getAssignedAreasCount() {
        return assignedAreasCount;
    }

    public void setAssignedAreasCount(int assignedAreasCount) {
        this.assignedAreasCount = assignedAreasCount;
    }
}
