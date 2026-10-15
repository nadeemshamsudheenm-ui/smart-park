package com.smartpark.service;

import com.smartpark.dto.FeeCalculationResponse;
import com.smartpark.dto.ReservationResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.PaymentRepository;
import com.smartpark.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingSessionServiceTest {

    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ParkingSlotRepository parkingSlotRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private StaffAssignmentService staffAssignmentService;
    @Mock
    private BillingService billingService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ParkingSessionService parkingSessionService;

    private User staff;
    private User customer;
    private ParkingArea area;
    private ParkingSlot slot;
    private Vehicle vehicle;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        staff = new User("Staff Member", "staff@smartpark.com", "hash", "555-0101", Role.STAFF);
        staff.setId(2L);

        customer = new User("Customer", "customer@smartpark.com", "hash", "555-0102", Role.CUSTOMER);
        customer.setId(3L);

        area = new ParkingArea();
        area.setId(1L);
        area.setName("Metro Central Plaza");

        slot = new ParkingSlot(area, "A-101", VehicleType.CAR, new BigDecimal("5.00"), SlotStatus.AVAILABLE);
        slot.setId(10L);

        vehicle = new Vehicle(customer, "CA-123-ABC", VehicleType.CAR);
        vehicle.setId(5L);

        reservation = new Reservation("SP-2026-CHECKIN", customer, vehicle, slot, LocalDateTime.now(), LocalDateTime.now().plusHours(2), ReservationStatus.CONFIRMED, "qr-checkin-uuid");
        reservation.setId(50L);
    }

    @Test
    @DisplayName("Staff check-in creates session, sets status ACTIVE, and marks slot OCCUPIED")
    void testCheckInSuccess() {
        when(reservationRepository.findByBookingReference("SP-2026-CHECKIN")).thenReturn(Optional.of(reservation));
        when(staffAssignmentService.isAuthorized(staff, 1L)).thenReturn(true);
        when(parkingSessionRepository.findByReservationId(50L)).thenReturn(Optional.empty());
        when(reservationService.enrichReservationResponse(any(Reservation.class))).thenReturn(new ReservationResponse());

        parkingSessionService.checkIn(staff, "SP-2026-CHECKIN");

        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(SlotStatus.OCCUPIED, slot.getStatus());
        verify(parkingSessionRepository, times(1)).save(any(ParkingSession.class));
        verify(parkingSlotRepository, times(1)).save(slot);
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    @DisplayName("Reject duplicate check-in if vehicle is already checked in")
    void testRejectDuplicateCheckIn() {
        reservation.setStatus(ReservationStatus.ACTIVE);
        when(reservationRepository.findByBookingReference("SP-2026-CHECKIN")).thenReturn(Optional.of(reservation));
        when(staffAssignmentService.isAuthorized(staff, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            parkingSessionService.checkIn(staff, "SP-2026-CHECKIN");
        });

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-out records exit time, computes fee, sets slot AVAILABLE and reservation COMPLETED")
    void testCheckOutSuccess() {
        reservation.setStatus(ReservationStatus.ACTIVE);
        slot.setStatus(SlotStatus.OCCUPIED);
        ParkingSession session = new ParkingSession(reservation, LocalDateTime.now().minusHours(2));
        session.setId(90L);

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(staffAssignmentService.isAuthorized(staff, 1L)).thenReturn(true);
        when(parkingSessionRepository.findByReservationId(50L)).thenReturn(Optional.of(session));
        when(billingService.calculateFee(any(), any(), any())).thenReturn(
                new FeeCalculationResponse(120, 2, new BigDecimal("5.00"), new BigDecimal("10.00"), "policy"));
        when(reservationService.enrichReservationResponse(any(Reservation.class))).thenReturn(new ReservationResponse());

        parkingSessionService.checkOut(staff, 50L);

        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertNotNull(session.getExitTime());
        assertEquals(new BigDecimal("10.00"), session.getCalculatedFee());
        verify(parkingSlotRepository, times(1)).save(slot);
        verify(reservationRepository, times(1)).save(reservation);
    }
}
