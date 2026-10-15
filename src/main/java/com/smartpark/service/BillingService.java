package com.smartpark.service;

import com.smartpark.dto.FeeCalculationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class BillingService {

    @Value("${smartpark.billing.minimum-charge-hours:1}")
    private long minimumChargeHours;

    @Value("${smartpark.billing.grace-period-minutes:15}")
    private long gracePeriodMinutes;

    public FeeCalculationResponse calculateFee(LocalDateTime startTime, LocalDateTime endTime, BigDecimal hourlyRate) {
        if (startTime == null || endTime == null || hourlyRate == null) {
            throw new IllegalArgumentException("Start time, end time, and hourly rate cannot be null");
        }

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 1) {
            durationMinutes = 1;
        }

        // Round up to next billable hour, with minimum charge guarantee
        long billableHours = (long) Math.ceil((double) durationMinutes / 60.0);
        if (billableHours < minimumChargeHours) {
            billableHours = minimumChargeHours;
        }

        BigDecimal calculatedFee = hourlyRate
                .multiply(BigDecimal.valueOf(billableHours))
                .setScale(2, RoundingMode.HALF_UP);

        String policy = String.format("Minimum %d hour(s) charge, rounded up to the nearest whole hour.", minimumChargeHours);

        return new FeeCalculationResponse(durationMinutes, billableHours, hourlyRate, calculatedFee, policy);
    }

    public BigDecimal calculateEstimatedFee(LocalDateTime startTime, LocalDateTime endTime, BigDecimal hourlyRate) {
        return calculateFee(startTime, endTime, hourlyRate).getCalculatedFee();
    }
}
