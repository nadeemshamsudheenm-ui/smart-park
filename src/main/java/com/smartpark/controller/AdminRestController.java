package com.smartpark.controller;

import com.smartpark.dto.ApiResponse;
import com.smartpark.dto.DashboardStatsDto;
import com.smartpark.dto.ReservationResponse;
import com.smartpark.dto.UserDto;
import com.smartpark.entity.AuditLog;
import com.smartpark.entity.Role;
import com.smartpark.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final StaffAssignmentService staffAssignmentService;
    private final ReservationService reservationService;
    private final AuditLogService auditLogService;

    public AdminRestController(DashboardService dashboardService,
                               UserService userService,
                               StaffAssignmentService staffAssignmentService,
                               ReservationService reservationService,
                               AuditLogService auditLogService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.staffAssignmentService = staffAssignmentService;
        this.reservationService = reservationService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard stats retrieved", stats));
    }

    @GetMapping("/reports/occupancy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOccupancyReport() {
        return ResponseEntity.ok(ApiResponse.success("Occupancy report data", dashboardService.getOccupancyReport()));
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueReport() {
        return ResponseEntity.ok(ApiResponse.success("Revenue report data", dashboardService.getRevenueReport()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", userService.getAllUsers()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        UserDto updated = userService.updateUserStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success("User status updated", updated));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        UserDto updated = userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated", updated));
    }

    @PostMapping("/staff-assignments")
    public ResponseEntity<ApiResponse<Void>> assignStaff(@RequestParam Long staffUserId, @RequestParam Long parkingAreaId) {
        staffAssignmentService.assignStaff(staffUserId, parkingAreaId);
        return ResponseEntity.ok(ApiResponse.success("Staff member assigned to parking area successfully"));
    }

    @DeleteMapping("/staff-assignments")
    public ResponseEntity<ApiResponse<Void>> removeStaffAssignment(@RequestParam Long staffUserId, @RequestParam Long parkingAreaId) {
        staffAssignmentService.removeAssignment(staffUserId, parkingAreaId);
        return ResponseEntity.ok(ApiResponse.success("Staff assignment removed successfully"));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations() {
        return ResponseEntity.ok(ApiResponse.success("All reservations retrieved", reservationService.getAllReservations()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", auditLogService.getRecentLogs()));
    }
}
