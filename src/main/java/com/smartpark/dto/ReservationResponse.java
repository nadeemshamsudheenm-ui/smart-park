package com.smartpark.dto;

import com.smartpark.entity.Reservation;
import com.smartpark.entity.ReservationStatus;
import com.smartpark.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private String bookingReference;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;

    private Long vehicleId;
    private String vehicleRegistration;
    private VehicleType vehicleType;

    private Long parkingAreaId;
    private String parkingAreaName;
    private String parkingAreaAddress;

    private Long parkingSlotId;
    private String slotNumber;
    private BigDecimal hourlyRate;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private String qrToken;
    private String qrCodeBase64;
    private BigDecimal estimatedFee;
    private LocalDateTime createdAt;

    // Session info if checked-in/out
    private boolean checkedIn;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private BigDecimal actualFee;

    // Payment info
    private String paymentStatus;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String transactionReference;

    public ReservationResponse() {
    }

    public static ReservationResponse fromEntity(Reservation reservation) {
        ReservationResponse res = new ReservationResponse();
        res.setId(reservation.getId());
        res.setBookingReference(reservation.getBookingReference());
        if (reservation.getUser() != null) {
            res.setUserId(reservation.getUser().getId());
            res.setUserFullName(reservation.getUser().getFullName());
            res.setUserEmail(reservation.getUser().getEmail());
            res.setUserPhone(reservation.getUser().getPhone());
        }
        if (reservation.getVehicle() != null) {
            res.setVehicleId(reservation.getVehicle().getId());
            res.setVehicleRegistration(reservation.getVehicle().getRegistrationNumber());
            res.setVehicleType(reservation.getVehicle().getVehicleType());
        }
        if (reservation.getParkingSlot() != null) {
            res.setParkingSlotId(reservation.getParkingSlot().getId());
            res.setSlotNumber(reservation.getParkingSlot().getSlotNumber());
            res.setHourlyRate(reservation.getParkingSlot().getHourlyRate());
            if (reservation.getParkingSlot().getParkingArea() != null) {
                res.setParkingAreaId(reservation.getParkingSlot().getParkingArea().getId());
                res.setParkingAreaName(reservation.getParkingSlot().getParkingArea().getName());
                res.setParkingAreaAddress(reservation.getParkingSlot().getParkingArea().getAddress());
            }
        }
        res.setStartTime(reservation.getStartTime());
        res.setEndTime(reservation.getEndTime());
        res.setStatus(reservation.getStatus());
        res.setQrToken(reservation.getQrToken());
        res.setCreatedAt(reservation.getCreatedAt());

        if (reservation.getParkingSession() != null) {
            res.setCheckedIn(true);
            res.setEntryTime(reservation.getParkingSession().getEntryTime());
            res.setExitTime(reservation.getParkingSession().getExitTime());
            res.setDurationMinutes(reservation.getParkingSession().getDurationMinutes());
            res.setActualFee(reservation.getParkingSession().getCalculatedFee());
        }

        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
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

    public String getParkingAreaAddress() {
        return parkingAreaAddress;
    }

    public void setParkingAreaAddress(String parkingAreaAddress) {
        this.parkingAreaAddress = parkingAreaAddress;
    }

    public Long getParkingSlotId() {
        return parkingSlotId;
    }

    public void setParkingSlotId(Long parkingSlotId) {
        this.parkingSlotId = parkingSlotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public BigDecimal getEstimatedFee() {
        return estimatedFee;
    }

    public void setEstimatedFee(BigDecimal estimatedFee) {
        this.estimatedFee = estimatedFee;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getActualFee() {
        return actualFee;
    }

    public void setActualFee(BigDecimal actualFee) {
        this.actualFee = actualFee;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
