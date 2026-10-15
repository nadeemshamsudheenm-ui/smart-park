package com.smartpark.service;

import com.smartpark.dto.ParkingSlotDto;
import com.smartpark.entity.ParkingArea;
import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.Reservation;
import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.VehicleType;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingAreaRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingAreaRepository parkingAreaRepository;
    private final ReservationRepository reservationRepository;
    private final AuditLogService auditLogService;

    public ParkingSlotService(ParkingSlotRepository parkingSlotRepository,
                              ParkingAreaRepository parkingAreaRepository,
                              ReservationRepository reservationRepository,
                              AuditLogService auditLogService) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingAreaRepository = parkingAreaRepository;
        this.reservationRepository = reservationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotDto> getAllSlots() {
        return parkingSlotRepository.findAll().stream()
                .map(ParkingSlotDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotDto> getSlotsByArea(Long areaId) {
        return parkingSlotRepository.findByParkingAreaId(areaId).stream()
                .map(ParkingSlotDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotDto> getAvailableSlotsForInterval(Long areaId, VehicleType vehicleType,
                                                             LocalDateTime startTime, LocalDateTime endTime) {
        List<ParkingSlot> slots = (vehicleType != null)
                ? parkingSlotRepository.findByParkingAreaIdAndVehicleType(areaId, vehicleType)
                : parkingSlotRepository.findByParkingAreaId(areaId);

        return slots.stream()
                .filter(slot -> slot.getStatus() != SlotStatus.INACTIVE)
                .map(slot -> {
                    ParkingSlotDto dto = ParkingSlotDto.fromEntity(slot);
                    // Check if overlapping reservations exist for this specific time window
                    List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
                            slot.getId(), startTime, endTime);
                    boolean isFree = overlaps.isEmpty() && (slot.getStatus() != SlotStatus.OCCUPIED);
                    dto.setCurrentlyAvailable(isFree);
                    // Dynamically set visual status for this time window
                    if (!overlaps.isEmpty()) {
                        dto.setStatus(SlotStatus.RESERVED);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingSlot getSlotEntityById(Long id) {
        return parkingSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Slot not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ParkingSlotDto getSlotById(Long id) {
        return ParkingSlotDto.fromEntity(getSlotEntityById(id));
    }

    @Transactional
    public ParkingSlotDto createSlot(ParkingSlotDto dto) {
        ParkingArea area = parkingAreaRepository.findById(dto.getParkingAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking area not found with id: " + dto.getParkingAreaId()));

        if (parkingSlotRepository.findByParkingAreaIdAndSlotNumber(area.getId(), dto.getSlotNumber().trim()).isPresent()) {
            throw new BadRequestException("Slot number " + dto.getSlotNumber() + " already exists in this area");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setParkingArea(area);
        slot.setSlotNumber(dto.getSlotNumber().trim().toUpperCase());
        slot.setVehicleType(dto.getVehicleType());
        slot.setHourlyRate(dto.getHourlyRate());
        slot.setStatus(dto.getStatus() != null ? dto.getStatus() : SlotStatus.AVAILABLE);

        ParkingSlot saved = parkingSlotRepository.save(slot);
        auditLogService.log(null, "CREATE_PARKING_SLOT", "Created slot " + saved.getSlotNumber() + " in area " + area.getName());
        return ParkingSlotDto.fromEntity(saved);
    }

    @Transactional
    public ParkingSlotDto updateSlot(Long id, ParkingSlotDto dto) {
        ParkingSlot slot = getSlotEntityById(id);
        slot.setSlotNumber(dto.getSlotNumber().trim().toUpperCase());
        slot.setVehicleType(dto.getVehicleType());
        slot.setHourlyRate(dto.getHourlyRate());
        if (dto.getStatus() != null) {
            slot.setStatus(dto.getStatus());
        }

        ParkingSlot saved = parkingSlotRepository.save(slot);
        auditLogService.log(null, "UPDATE_PARKING_SLOT", "Updated slot id: " + id);
        return ParkingSlotDto.fromEntity(saved);
    }

    @Transactional
    public ParkingSlotDto updateSlotStatus(Long id, SlotStatus status) {
        ParkingSlot slot = getSlotEntityById(id);
        slot.setStatus(status);
        ParkingSlot saved = parkingSlotRepository.save(slot);
        auditLogService.log(null, "UPDATE_SLOT_STATUS", "Updated slot " + slot.getSlotNumber() + " status to " + status);
        return ParkingSlotDto.fromEntity(saved);
    }

    @Transactional
    public void deleteSlot(Long id) {
        ParkingSlot slot = getSlotEntityById(id);
        parkingSlotRepository.delete(slot);
        auditLogService.log(null, "DELETE_PARKING_SLOT", "Deleted slot id: " + id);
    }
}
