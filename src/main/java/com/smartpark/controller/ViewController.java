package com.smartpark.controller;

import com.smartpark.dto.ReservationResponse;
import com.smartpark.security.CustomUserDetails;
import com.smartpark.service.ReservationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    private final ReservationService reservationService;

    public ViewController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            switch (userDetails.getUser().getRole()) {
                case ADMIN: return "redirect:/admin/dashboard";
                case STAFF: return "redirect:/staff/dashboard";
                case CUSTOMER: return "redirect:/customer/dashboard";
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    // Customer Views
    @GetMapping("/customer/dashboard")
    public String customerDashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "customer/dashboard";
    }

    @GetMapping("/customer/book-slot")
    public String customerBookSlot(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "customer/book-slot";
    }

    @GetMapping("/customer/reservations")
    public String customerReservations(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "customer/reservations";
    }

    @GetMapping("/customer/vehicles")
    public String customerVehicles(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "customer/vehicles";
    }

    @GetMapping("/customer/payments")
    public String customerPayments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "customer/payments";
    }

    @GetMapping("/customer/receipt/{id}")
    public String customerReceipt(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        ReservationResponse reservation = reservationService.getReservationById(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("user", user);
        return "customer/receipt";
    }

    // Staff Views
    @GetMapping("/staff/dashboard")
    public String staffDashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "staff/dashboard";
    }

    @GetMapping("/staff/verify")
    public String staffVerify(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "staff/verify";
    }

    @GetMapping("/staff/live-sessions")
    public String staffLiveSessions(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "staff/live-sessions";
    }

    // Admin Views
    @GetMapping("/admin/dashboard")
    public String adminDashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/dashboard";
    }

    @GetMapping("/admin/areas")
    public String adminAreas(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/areas";
    }

    @GetMapping("/admin/slots")
    public String adminSlots(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/slots";
    }

    @GetMapping("/admin/users")
    public String adminUsers(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/users";
    }

    @GetMapping("/admin/reservations")
    public String adminReservations(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/reservations";
    }

    @GetMapping("/admin/payments")
    public String adminPayments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/payments";
    }

    @GetMapping("/admin/reports")
    public String adminReports(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/reports";
    }

    @GetMapping("/admin/audit-logs")
    public String adminAuditLogs(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("user", user);
        return "admin/audit-logs";
    }

    // Error Views
    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
