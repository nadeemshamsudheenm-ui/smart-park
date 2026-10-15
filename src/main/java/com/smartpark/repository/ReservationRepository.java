package com.smartpark.repository;

import com.smartpark.entity.Reservation;
import com.smartpark.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByBookingReference(String bookingReference);

    Optional<Reservation> findByQrToken(String qrToken);

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findByParkingSlotParkingAreaIdOrderByStartTimeDesc(Long parkingAreaId);

    List<Reservation> findByStatus(ReservationStatus status);

    long countByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.parkingSlot.id = :slotId " +
           "AND r.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') " +
           "AND (:startTime < r.endTime AND :endTime > r.startTime)")
    List<Reservation> findOverlappingReservations(
            @Param("slotId") Long slotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT r FROM Reservation r WHERE r.parkingSlot.id = :slotId " +
           "AND r.id != :reservationId " +
           "AND r.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') " +
           "AND (:startTime < r.endTime AND :endTime > r.startTime)")
    List<Reservation> findOverlappingReservationsExcluding(
            @Param("slotId") Long slotId,
            @Param("reservationId") Long reservationId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT r FROM Reservation r WHERE r.parkingSlot.parkingArea.id = :areaId " +
           "AND r.startTime BETWEEN :startOfDay AND :endOfDay " +
           "ORDER BY r.startTime ASC")
    List<Reservation> findByParkingAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT r FROM Reservation r WHERE r.parkingSlot.parkingArea.id IN :areaIds " +
           "AND r.startTime BETWEEN :startOfDay AND :endOfDay " +
           "ORDER BY r.startTime ASC")
    List<Reservation> findByParkingAreaIdsAndDate(
            @Param("areaIds") List<Long> areaIds,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.endTime >= :now " +
           "ORDER BY r.startTime ASC")
    List<Reservation> findUpcomingReservationsForUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );
}
