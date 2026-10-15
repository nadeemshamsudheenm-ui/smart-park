package com.smartpark.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "staff_assignments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_staff_area", columnNames = {"staff_user_id", "parking_area_id"})
})
public class StaffAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private User staffUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parking_area_id", nullable = false)
    private ParkingArea parkingArea;

    public StaffAssignment() {
    }

    public StaffAssignment(User staffUser, ParkingArea parkingArea) {
        this.staffUser = staffUser;
        this.parkingArea = parkingArea;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStaffUser() {
        return staffUser;
    }

    public void setStaffUser(User staffUser) {
        this.staffUser = staffUser;
    }

    public ParkingArea getParkingArea() {
        return parkingArea;
    }

    public void setParkingArea(ParkingArea parkingArea) {
        this.parkingArea = parkingArea;
    }
}
