package com.smartpark.controller;

import com.smartpark.dto.ApiResponse;
import com.smartpark.dto.ReservationRequest;
import com.smartpark.dto.ReservationResponse;
import com.smartpark.entity.Role;
import com.smartpark.security.CustomUserDetails;
import com.smartpark.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation confirmed successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReservationResponse> reservations = reservationService.getReservationsByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("User reservations retrieved", reservations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ReservationResponse res = reservationService.getReservationById(id);
        boolean isAdminOrStaff = userDetails.getUser().getRole() == Role.ADMIN || userDetails.getUser().getRole() == Role.STAFF;
        if (!isAdminOrStaff && !res.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You are not authorized to view this reservation"));
        }
        return ResponseEntity.ok(ApiResponse.success("Reservation details", res));
    }

    @GetMapping("/ref/{reference}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationByReference(
            @PathVariable String reference) {
        ReservationResponse res = reservationService.getReservationByReference(reference);
        return ResponseEntity.ok(ApiResponse.success("Reservation found", res));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        boolean isAdmin = userDetails.getUser().getRole() == Role.ADMIN;
        ReservationResponse cancelled = reservationService.cancelReservation(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully", cancelled));
    }
}
