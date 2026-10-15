package com.smartpark.dto;

import jakarta.validation.constraints.NotNull;

public class CheckOutRequest {

    @NotNull(message = "Reservation ID is required")
    private Long reservationId;

    public CheckOutRequest() {
    }

    public CheckOutRequest(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
}
