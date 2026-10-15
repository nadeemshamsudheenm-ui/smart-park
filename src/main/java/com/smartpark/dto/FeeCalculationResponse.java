package com.smartpark.dto;

import java.math.BigDecimal;

public class FeeCalculationResponse {

    private long durationMinutes;
    private long billableHours;
    private BigDecimal hourlyRate;
    private BigDecimal calculatedFee;
    private String billingPolicy;

    public FeeCalculationResponse() {
    }

    public FeeCalculationResponse(long durationMinutes, long billableHours, BigDecimal hourlyRate, BigDecimal calculatedFee, String billingPolicy) {
        this.durationMinutes = durationMinutes;
        this.billableHours = billableHours;
        this.hourlyRate = hourlyRate;
        this.calculatedFee = calculatedFee;
        this.billingPolicy = billingPolicy;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public long getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(long billableHours) {
        this.billableHours = billableHours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getCalculatedFee() {
        return calculatedFee;
    }

    public void setCalculatedFee(BigDecimal calculatedFee) {
        this.calculatedFee = calculatedFee;
    }

    public String getBillingPolicy() {
        return billingPolicy;
    }

    public void setBillingPolicy(String billingPolicy) {
        this.billingPolicy = billingPolicy;
    }
}
