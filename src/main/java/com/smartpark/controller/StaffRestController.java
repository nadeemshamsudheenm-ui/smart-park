package com.smartpark.controller;

import com.smartpark.dto.*;
import com.smartpark.entity.User;
import com.smartpark.security.CustomUserDetails;
import com.smartpark.service.DashboardService;
import com.smartpark.service.ParkingSessionService;
import com.smartpark.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffRestController {

    private final ParkingSessionService parkingSessionService;
    private final ReservationService reservationService;
    private final DashboardService dashboardService;

    public StaffRestController(ParkingSessionService parkingSessionService,
                               ReservationService reservationService,
                               DashboardService dashboardService) {
        this.parkingSessionService = parkingSessionService;
        this.reservationService = reservationService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<StaffDashboardStatsDto>> getStaffDashboardStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StaffDashboardStatsDto stats = dashboardService.getStaffDashboardStats(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Staff stats retrieved", stats));
    }

    @PostMapping("/verify-booking")
    public ResponseEntity<ApiResponse<ReservationResponse>> verifyBooking(
            @Valid @RequestBody CheckInRequest request) {
        String identifier = request.getIdentifier().trim();
        ReservationResponse res;
        try {
            res = reservationService.getReservationByReference(identifier);
        } catch (Exception e) {
            res = reservationService.getReservationByQrToken(identifier);
        }
        return ResponseEntity.ok(ApiResponse.success("Booking verified", res));
    }

    @PostMapping("/sessions/check-in")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkIn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckInRequest request) {
        User staffUser = userDetails.getUser();
        ReservationResponse checkedIn = parkingSessionService.checkIn(staffUser, request.getIdentifier());
        return ResponseEntity.ok(ApiResponse.success("Vehicle entry recorded successfully", checkedIn));
    }

    @PostMapping("/sessions/check-out")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkOut(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckOutRequest request) {
        User staffUser = userDetails.getUser();
        ReservationResponse checkedOut = parkingSessionService.checkOut(staffUser, request.getReservationId());
        return ResponseEntity.ok(ApiResponse.success("Vehicle exit recorded and billing calculated", checkedOut));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getActiveSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReservationResponse> sessions = parkingSessionService.getActiveSessionsForStaff(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Active parking sessions retrieved", sessions));
    }
}
