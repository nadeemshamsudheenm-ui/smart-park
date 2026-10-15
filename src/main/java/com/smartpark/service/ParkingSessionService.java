package com.smartpark.service;

import com.smartpark.dto.FeeCalculationResponse;
import com.smartpark.dto.ReservationResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.PaymentRepository;
import com.smartpark.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final PaymentRepository paymentRepository;
    private final StaffAssignmentService staffAssignmentService;
    private final BillingService billingService;
    private final AuditLogService auditLogService;
    private final ReservationService reservationService;

    public ParkingSessionService(ParkingSessionRepository parkingSessionRepository,
                                 ReservationRepository reservationRepository,
                                 ParkingSlotRepository parkingSlotRepository,
                                 PaymentRepository paymentRepository,
                                 StaffAssignmentService staffAssignmentService,
                                 BillingService billingService,
                                 AuditLogService auditLogService,
                                 ReservationService reservationService) {
        this.parkingSessionRepository = parkingSessionRepository;
        this.reservationRepository = reservationRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.paymentRepository = paymentRepository;
        this.staffAssignmentService = staffAssignmentService;
        this.billingService = billingService;
        this.auditLogService = auditLogService;
        this.reservationService = reservationService;
    }

    @Transactional
    public ReservationResponse checkIn(User staffUser, String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new BadRequestException("Booking reference or QR token is required.");
        }

        String trimmed = identifier.trim();
        Reservation reservation = reservationRepository.findByBookingReference(trimmed)
                .or(() -> reservationRepository.findByQrToken(trimmed))
                .orElseThrow(() -> new ResourceNotFoundException("No booking found matching reference or QR token: " + trimmed));

        // Area authorization check for staff
        Long areaId = reservation.getParkingSlot().getParkingArea().getId();
        if (!staffAssignmentService.isAuthorized(staffUser, areaId)) {
            throw new BadRequestException("Unauthorized: You are not assigned to manage parking area '" +
                    reservation.getParkingSlot().getParkingArea().getName() + "'.");
        }

        // Validate booking status
        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            throw new BadRequestException("Vehicle is already checked in for booking: " + reservation.getBookingReference());
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BadRequestException("Booking has already been completed: " + reservation.getBookingReference());
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Cannot check in: Booking has been cancelled.");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Booking is not in a valid state for entry.");
        }

        // Prevent duplicate session
        if (parkingSessionRepository.findByReservationId(reservation.getId()).isPresent()) {
            throw new BadRequestException("A parking session already exists for this booking.");
        }

        LocalDateTime now = LocalDateTime.now();
        ParkingSession session = new ParkingSession(reservation, now);
        parkingSessionRepository.save(session);

        reservation.setStatus(ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        // Update slot status to OCCUPIED
        ParkingSlot slot = reservation.getParkingSlot();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        auditLogService.log(staffUser, "CHECK_IN",
                "Checked in vehicle " + reservation.getVehicle().getRegistrationNumber() +
                " for booking " + reservation.getBookingReference() + " at " + now);

        return reservationService.enrichReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse checkOut(User staffUser, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        Long areaId = reservation.getParkingSlot().getParkingArea().getId();
        if (!staffAssignmentService.isAuthorized(staffUser, areaId)) {
            throw new BadRequestException("Unauthorized: You are not assigned to manage this parking area.");
        }

        ParkingSession session = parkingSessionRepository.findByReservationId(reservation.getId())
                .orElseThrow(() -> new BadRequestException("No active parking session found for this booking."));

        if (session.getExitTime() != null) {
            throw new BadRequestException("Vehicle has already checked out.");
        }

        LocalDateTime exitTime = LocalDateTime.now();
        session.setExitTime(exitTime);

        // Calculate actual duration and final fee
        FeeCalculationResponse feeCalc = billingService.calculateFee(
                session.getEntryTime(), exitTime, reservation.getParkingSlot().getHourlyRate());

        session.setDurationMinutes(feeCalc.getDurationMinutes());
        session.setCalculatedFee(feeCalc.getCalculatedFee());
        parkingSessionRepository.save(session);

        // Mark reservation COMPLETED
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);

        // Update slot back to AVAILABLE
        ParkingSlot slot = reservation.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        // Update payment with the final calculated fee
        Payment payment = paymentRepository.findByReservationId(reservation.getId())
                .orElseGet(() -> new Payment(
                        reservation,
                        feeCalc.getCalculatedFee(),
                        PaymentMethod.DEMO_PAYMENT,
                        "TXN-" + reservation.getBookingReference(),
                        PaymentStatus.PENDING
                ));
        payment.setAmount(feeCalc.getCalculatedFee());
        paymentRepository.save(payment);

        auditLogService.log(staffUser, "CHECK_OUT",
                "Checked out vehicle " + reservation.getVehicle().getRegistrationNumber() +
                " - Duration: " + feeCalc.getDurationMinutes() + " mins, Fee: ₹" + feeCalc.getCalculatedFee());

        return reservationService.enrichReservationResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getActiveSessions() {
        return parkingSessionRepository.findByExitTimeIsNull().stream()
                .map(s -> reservationService.enrichReservationResponse(s.getReservation()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getActiveSessionsForStaff(Long staffUserId) {
        List<Long> areaIds = staffAssignmentService.getAssignedAreaIds(staffUserId);
        if (areaIds.isEmpty()) {
            return List.of();
        }
        return parkingSessionRepository.findByReservationParkingSlotParkingAreaIdInAndExitTimeIsNull(areaIds).stream()
                .map(s -> reservationService.enrichReservationResponse(s.getReservation()))
                .collect(Collectors.toList());
    }
}
