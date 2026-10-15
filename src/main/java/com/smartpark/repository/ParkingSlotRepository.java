package com.smartpark.repository;

import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByParkingAreaId(Long parkingAreaId);
    List<ParkingSlot> findByStatus(SlotStatus status);
    List<ParkingSlot> findByParkingAreaIdAndStatus(Long parkingAreaId, SlotStatus status);
    List<ParkingSlot> findByParkingAreaIdAndVehicleType(Long parkingAreaId, VehicleType vehicleType);
    Optional<ParkingSlot> findByParkingAreaIdAndSlotNumber(Long parkingAreaId, String slotNumber);
    long countByStatus(SlotStatus status);
    long countByParkingAreaId(Long parkingAreaId);
}
