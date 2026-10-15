package com.smartpark.service;

import com.smartpark.dto.DashboardStatsDto;
import com.smartpark.dto.StaffDashboardStatsDto;
import com.smartpark.entity.*;
import com.smartpark.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class DashboardService {

    private final ParkingAreaRepository parkingAreaRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final PaymentRepository paymentRepository;
    private final StaffAssignmentService staffAssignmentService;

    public DashboardService(ParkingAreaRepository parkingAreaRepository,
                            ParkingSlotRepository parkingSlotRepository,
                            ReservationRepository reservationRepository,
                            ParkingSessionRepository parkingSessionRepository,
                            PaymentRepository paymentRepository,
                            StaffAssignmentService staffAssignmentService) {
        this.parkingAreaRepository = parkingAreaRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.reservationRepository = reservationRepository;
        this.parkingSessionRepository = parkingSessionRepository;
        this.paymentRepository = paymentRepository;
        this.staffAssignmentService = staffAssignmentService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getAdminDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalParkingAreas(parkingAreaRepository.count());
        stats.setTotalSlots(parkingSlotRepository.count());
        stats.setAvailableSlots(parkingSlotRepository.countByStatus(SlotStatus.AVAILABLE));
        stats.setOccupiedSlots(parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED));
        stats.setReservedSlots(parkingSlotRepository.countByStatus(SlotStatus.RESERVED));
        stats.setInactiveSlots(parkingSlotRepository.countByStatus(SlotStatus.INACTIVE));
        stats.setUpcomingReservations(reservationRepository.countByStatus(ReservationStatus.CONFIRMED));
        stats.setActiveParkingSessions(parkingSessionRepository.countByExitTimeIsNull());
        stats.setTotalCompletedBookings(reservationRepository.countByStatus(ReservationStatus.COMPLETED));

        BigDecimal totalRev = paymentRepository.calculateTotalRevenue();
        stats.setTotalRevenue(totalRev != null ? totalRev : BigDecimal.ZERO);

        return stats;
    }

    @Transactional(readOnly = true)
    public StaffDashboardStatsDto getStaffDashboardStats(Long staffUserId) {
        StaffDashboardStatsDto stats = new StaffDashboardStatsDto();
        List<Long> assignedAreaIds = staffAssignmentService.getAssignedAreaIds(staffUserId);
        stats.setAssignedAreasCount(assignedAreaIds.size());

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Reservation> todayReservations = assignedAreaIds.isEmpty()
                ? List.of()
                : reservationRepository.findByParkingAreaIdsAndDate(assignedAreaIds, startOfDay, endOfDay);

        stats.setTodayReservations(todayReservations.size());
        stats.setExpectedArrivals(todayReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count());

        long activeSessions = assignedAreaIds.isEmpty() ? 0 :
                parkingSessionRepository.findByReservationParkingSlotParkingAreaIdInAndExitTimeIsNull(assignedAreaIds).size();
        stats.setCurrentlyParked(activeSessions);

        long completedSessions = todayReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .count();
        stats.setCompletedSessions(completedSessions);

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOccupancyReport() {
        List<ParkingArea> areas = parkingAreaRepository.findAll();
        List<String> labels = new ArrayList<>();
        List<Integer> totalSlots = new ArrayList<>();
        List<Integer> availableSlots = new ArrayList<>();
        List<Integer> occupiedSlots = new ArrayList<>();

        for (ParkingArea area : areas) {
            labels.add(area.getName());
            int total = area.getSlots() != null ? area.getSlots().size() : 0;
            int avail = (int) (area.getSlots() != null ? area.getSlots().stream()
                    .filter(s -> s.getStatus() == SlotStatus.AVAILABLE).count() : 0);
            int occ = (int) (area.getSlots() != null ? area.getSlots().stream()
                    .filter(s -> s.getStatus() == SlotStatus.OCCUPIED || s.getStatus() == SlotStatus.RESERVED).count() : 0);

            totalSlots.add(total);
            availableSlots.add(avail);
            occupiedSlots.add(occ);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("totalSlots", totalSlots);
        result.put("availableSlots", availableSlots);
        result.put("occupiedSlots", occupiedSlots);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueReport() {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.toString());
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            BigDecimal dayRev = paymentRepository.calculateRevenueBetween(start, end);
            amounts.add(dayRev != null ? dayRev : BigDecimal.ZERO);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("amounts", amounts);
        return result;
    }
}
