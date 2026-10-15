package com.smartpark.service;

import com.smartpark.dto.FeeCalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BillingServiceTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService();
        ReflectionTestUtils.setField(billingService, "minimumChargeHours", 1L);
        ReflectionTestUtils.setField(billingService, "gracePeriodMinutes", 15L);
    }

    @Test
    @DisplayName("Short duration (20 mins) must be billed minimum 1 hour")
    void testMinimumCharge() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 14, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 14, 10, 20);
        BigDecimal hourlyRate = new BigDecimal("5.00");

        FeeCalculationResponse fee = billingService.calculateFee(start, end, hourlyRate);

        assertEquals(20, fee.getDurationMinutes());
        assertEquals(1, fee.getBillableHours());
        assertEquals(new BigDecimal("5.00"), fee.getCalculatedFee());
    }

    @Test
    @DisplayName("75 minutes duration should round up to 2 billable hours")
    void testRoundingUpToNextHour() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 14, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 14, 11, 15);
        BigDecimal hourlyRate = new BigDecimal("4.50");

        FeeCalculationResponse fee = billingService.calculateFee(start, end, hourlyRate);

        assertEquals(75, fee.getDurationMinutes());
        assertEquals(2, fee.getBillableHours());
        assertEquals(new BigDecimal("9.00"), fee.getCalculatedFee());
    }

    @Test
    @DisplayName("Invalid interval where end is before start throws exception")
    void testInvalidTimeInterval() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 14, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 14, 11, 0);
        BigDecimal hourlyRate = new BigDecimal("5.00");

        assertThrows(IllegalArgumentException.class, () -> {
            billingService.calculateFee(start, end, hourlyRate);
        });
    }
}
