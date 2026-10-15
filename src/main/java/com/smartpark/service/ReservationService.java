package com.smartpark.service;

import com.smartpark.dto.ReservationRequest;
import com.smartpark.dto.ReservationResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.exception.SlotNotAvailableException;
import com.smartpark.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final BillingService billingService;
    private final QrCodeService qrCodeService;
    private final AuditLogService auditLogService;

    private static final String REF_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public ReservationService(ReservationRepository reservationRepository,
                              ParkingSlotRepository parkingSlotRepository,
                              VehicleRepository vehicleRepository,
                              PaymentRepository paymentRepository,
                              UserService userService,
                              BillingService billingService,
                              QrCodeService qrCodeService,
                              AuditLogService auditLogService) {
        this.reservationRepository = reservationRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.vehicleRepository = vehicleRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.billingService = billingService;
        this.qrCodeService = qrCodeService;
        this.auditLogService = auditLogService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {
        User user = userService.getUserById(userId);

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + request.getVehicleId()));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new BadRequestException("Selected vehicle does not belong to your account.");
        }

        ParkingSlot slot = parkingSlotRepository.findById(request.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + request.getParkingSlotId()));

        if (slot.getStatus() == SlotStatus.INACTIVE) {
            throw new BadRequestException("The selected parking slot is currently inactive.");
        }

        // Validate vehicle compatibility
        if (slot.getVehicleType() != vehicle.getVehicleType()) {
            throw new BadRequestException("Slot vehicle compatibility (" + slot.getVehicleType() +
                    ") does not match your vehicle (" + vehicle.getVehicleType() + ").");
        }

        // Validate times
        LocalDateTime now = LocalDateTime.now();
        if (request.getStartTime().isBefore(now.minusMinutes(5))) {
            throw new BadRequestException("Reservation start time cannot be in the past.");
        }
        if (request.getEndTime().isBefore(request.getStartTime().plusMinutes(15))) {
            throw new BadRequestException("Reservation end time must be at least 15 minutes after start time.");
        }

        // Overlap verification to prevent double bookings
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                slot.getId(), request.getStartTime(), request.getEndTime());
        if (!overlapping.isEmpty()) {
            throw new SlotNotAvailableException("Slot " + slot.getSlotNumber() + " is already reserved for the chosen time window.");
        }

        // Generate booking reference & opaque QR token
        String bookingRef = generateBookingReference();
        String qrToken = UUID.randomUUID().toString();

        Reservation reservation = new Reservation(
                bookingRef,
                user,
                vehicle,
                slot,
                request.getStartTime(),
                request.getEndTime(),
                ReservationStatus.CONFIRMED,
                qrToken
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        // Calculate estimated fee
        BigDecimal estimatedFee = billingService.calculateEstimatedFee(
                request.getStartTime(), request.getEndTime(), slot.getHourlyRate());

        // Create initial pending payment record
        Payment payment = new Payment(
                savedReservation,
                estimatedFee,
                PaymentMethod.DEMO_PAYMENT,
                "TXN-" + bookingRef,
                PaymentStatus.PENDING
        );
        paymentRepository.save(payment);

        auditLogService.log(user, "CREATE_RESERVATION",
                "Created reservation " + bookingRef + " for slot " + slot.getSlotNumber());

        return enrichReservationResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        return enrichReservationResponse(getReservationEntityById(id));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationByReference(String reference) {
        Reservation reservation = reservationRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with reference: " + reference));
        return enrichReservationResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationByQrToken(String qrToken) {
        Reservation reservation = reservationRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with QR token."));
        return enrichReservationResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::enrichReservationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::enrichReservationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, Long userId, boolean isAdmin) {
        Reservation reservation = getReservationEntityById(reservationId);

        if (!isAdmin && !reservation.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own reservations.");
        }

        if (reservation.getStatus() == ReservationStatus.ACTIVE || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel an active or already completed parking session.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("This reservation is already cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        // Cancel associated pending payment
        paymentRepository.findByReservationId(reservationId).ifPresent(p -> {
            if (p.getPaymentStatus() == PaymentStatus.PENDING) {
                p.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(p);
            }
        });

        auditLogService.log(reservation.getUser(), "CANCEL_RESERVATION",
                "Cancelled reservation: " + reservation.getBookingReference());

        return enrichReservationResponse(saved);
    }

    public ReservationResponse enrichReservationResponse(Reservation res) {
        ReservationResponse dto = ReservationResponse.fromEntity(res);

        // Calculate estimated fee
        BigDecimal estimatedFee = billingService.calculateEstimatedFee(
                res.getStartTime(), res.getEndTime(), res.getParkingSlot().getHourlyRate());
        dto.setEstimatedFee(estimatedFee);

        // Generate QR code base64 from secure token
        try {
            String qrBase64 = qrCodeService.generateQrCodeBase64(res.getQrToken(), 250, 250);
            dto.setQrCodeBase64(qrBase64);
        } catch (Exception ignored) {
        }

        // Attach payment info if available
        paymentRepository.findByReservationId(res.getId()).ifPresent(p -> {
            dto.setPaymentStatus(p.getPaymentStatus().name());
            dto.setPaymentAmount(p.getAmount());
            dto.setPaymentMethod(p.getPaymentMethod().name());
            dto.setTransactionReference(p.getTransactionReference());
        });

        return dto;
    }

    private String generateBookingReference() {
        StringBuilder sb = new StringBuilder("SP-");
        sb.append(LocalDateTime.now().getYear()).append("-");
        for (int i = 0; i < 6; i++) {
            sb.append(REF_CHARS.charAt(RANDOM.nextInt(REF_CHARS.length())));
        }
        return sb.toString();
    }
}
