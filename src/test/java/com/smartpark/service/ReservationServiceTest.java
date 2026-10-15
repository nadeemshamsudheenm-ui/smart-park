package com.smartpark.service;

import com.smartpark.dto.ReservationRequest;
import com.smartpark.dto.ReservationResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.SlotNotAvailableException;
import com.smartpark.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ParkingSlotRepository parkingSlotRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserService userService;
    @Mock
    private BillingService billingService;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReservationService reservationService;

    private User customer;
    private Vehicle vehicle;
    private ParkingArea area;
    private ParkingSlot slot;

    @BeforeEach
    void setUp() {
        customer = new User("Jane Customer", "jane@example.com", "hash", "555-0199", Role.CUSTOMER);
        customer.setId(10L);

        vehicle = new Vehicle(customer, "XYZ-123", VehicleType.CAR);
        vehicle.setId(20L);

        area = new ParkingArea();
        area.setId(1L);
        area.setName("Downtown Plaza");

        slot = new ParkingSlot(area, "A-101", VehicleType.CAR, new BigDecimal("5.00"), SlotStatus.AVAILABLE);
        slot.setId(30L);
    }

    @Test
    @DisplayName("Successfully create reservation when slot is free")
    void testCreateReservationSuccess() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest req = new ReservationRequest(vehicle.getId(), slot.getId(), start, end);

        when(userService.getUserById(10L)).thenReturn(customer);
        when(vehicleRepository.findById(20L)).thenReturn(Optional.of(vehicle));
        when(parkingSlotRepository.findById(30L)).thenReturn(Optional.of(slot));
        when(reservationRepository.findOverlappingReservations(30L, start, end)).thenReturn(List.of());
        when(billingService.calculateEstimatedFee(start, end, slot.getHourlyRate())).thenReturn(new BigDecimal("10.00"));

        Reservation savedReservation = new Reservation("SP-2026-TEST01", customer, vehicle, slot, start, end, ReservationStatus.CONFIRMED, "qr-uuid-test");
        savedReservation.setId(100L);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(qrCodeService.generateQrCodeBase64(anyString(), anyInt(), anyInt())).thenReturn("fakeBase64String");

        ReservationResponse response = reservationService.createReservation(10L, req);

        assertNotNull(response);
        assertEquals("SP-2026-TEST01", response.getBookingReference());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        assertEquals("fakeBase64String", response.getQrCodeBase64());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Reject reservation when overlapping booking exists on same slot")
    void testRejectOverlappingReservation() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(3);
        ReservationRequest req = new ReservationRequest(vehicle.getId(), slot.getId(), start, end);

        when(userService.getUserById(10L)).thenReturn(customer);
        when(vehicleRepository.findById(20L)).thenReturn(Optional.of(vehicle));
        when(parkingSlotRepository.findById(30L)).thenReturn(Optional.of(slot));

        Reservation existingReservation = new Reservation("SP-EXISTING", customer, vehicle, slot, start, end, ReservationStatus.CONFIRMED, "qr-existing");
        when(reservationRepository.findOverlappingReservations(30L, start, end)).thenReturn(List.of(existingReservation));

        assertThrows(SlotNotAvailableException.class, () -> {
            reservationService.createReservation(10L, req);
        });

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancel reservation updates status to CANCELLED")
    void testCancelReservationSuccess() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);
        Reservation res = new Reservation("SP-CANCEL-TEST", customer, vehicle, slot, start, end, ReservationStatus.CONFIRMED, "qr-cancel");
        res.setId(105L);

        when(reservationRepository.findById(105L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse cancelled = reservationService.cancelReservation(105L, 10L, false);

        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus());
        verify(reservationRepository, times(1)).save(res);
    }

    @Test
    @DisplayName("Cannot cancel an active parking session")
    void testCannotCancelActiveSession() {
        Reservation res = new Reservation("SP-ACTIVE-TEST", customer, vehicle, slot, LocalDateTime.now(), LocalDateTime.now().plusHours(2), ReservationStatus.ACTIVE, "qr-active");
        res.setId(106L);

        when(reservationRepository.findById(106L)).thenReturn(Optional.of(res));

        assertThrows(BadRequestException.class, () -> {
            reservationService.cancelReservation(106L, 10L, false);
        });
    }
}
