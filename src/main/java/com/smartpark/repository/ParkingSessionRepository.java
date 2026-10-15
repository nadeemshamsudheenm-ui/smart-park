package com.smartpark.repository;

import com.smartpark.entity.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    Optional<ParkingSession> findByReservationId(Long reservationId);
    List<ParkingSession> findByExitTimeIsNull();
    long countByExitTimeIsNull();
    List<ParkingSession> findByReservationParkingSlotParkingAreaIdAndExitTimeIsNull(Long areaId);
    List<ParkingSession> findByReservationParkingSlotParkingAreaIdInAndExitTimeIsNull(List<Long> areaIds);
    List<ParkingSession> findTop20ByOrderByEntryTimeDesc();
}
