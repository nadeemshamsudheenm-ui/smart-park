package com.smartpark.controller;

import com.smartpark.dto.ApiResponse;
import com.smartpark.dto.DemoPaymentRequest;
import com.smartpark.entity.Payment;
import com.smartpark.entity.Role;
import com.smartpark.security.CustomUserDetails;
import com.smartpark.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/demo")
    public ResponseEntity<ApiResponse<Payment>> processDemoPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DemoPaymentRequest request) {
        boolean isAdmin = userDetails.getUser().getRole() == Role.ADMIN;
        Payment payment = paymentService.processDemoPayment(userDetails.getId(), request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully (Demo Mode)", payment));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Payment>>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", payments));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("All payments retrieved", paymentService.getAllPayments()));
    }
}
