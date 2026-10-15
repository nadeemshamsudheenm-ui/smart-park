package com.smartpark.service;

import com.smartpark.dto.DemoPaymentRequest;
import com.smartpark.entity.Payment;
import com.smartpark.entity.PaymentMethod;
import com.smartpark.entity.PaymentStatus;
import com.smartpark.entity.Reservation;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.PaymentRepository;
import com.smartpark.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final AuditLogService auditLogService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository,
                          AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Payment processDemoPayment(Long userId, DemoPaymentRequest request, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + request.getReservationId()));

        if (!isAdmin && !reservation.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only pay for your own reservations.");
        }

        Payment payment = paymentRepository.findByReservationId(reservation.getId())
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setReservation(reservation);
                    return p;
                });

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("This booking has already been paid for.");
        }

        BigDecimal payAmount = request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.getAmount()
                : (payment.getAmount() != null ? payment.getAmount() : BigDecimal.valueOf(10.00));

        payment.setAmount(payAmount);
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.DEMO_PAYMENT);
        payment.setTransactionReference("TXN-DEMO-" + System.currentTimeMillis() + "-" + (1000 + RANDOM.nextInt(9000)));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        auditLogService.log(reservation.getUser(), "PAYMENT_COMPLETED",
                "Completed payment ₹" + payAmount + " for booking " + reservation.getBookingReference() +
                " (Method: " + payment.getPaymentMethod() + ", Ref: " + saved.getTransactionReference() + ")");

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByReservationUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return paymentRepository.calculateTotalRevenue();
    }
}
