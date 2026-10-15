package com.smartpark.dto;

import com.smartpark.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DemoPaymentRequest {

    @NotNull(message = "Reservation ID is required")
    private Long reservationId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    public DemoPaymentRequest() {
    }

    public DemoPaymentRequest(Long reservationId, PaymentMethod paymentMethod, BigDecimal amount) {
        this.reservationId = reservationId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
