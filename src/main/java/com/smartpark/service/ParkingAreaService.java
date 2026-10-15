package com.smartpark.service;

import com.smartpark.dto.ParkingAreaDto;
import com.smartpark.entity.ParkingArea;
import com.smartpark.entity.SlotStatus;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingAreaRepository;
import com.smartpark.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingAreaService {

    private final ParkingAreaRepository parkingAreaRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final AuditLogService auditLogService;

    public ParkingAreaService(ParkingAreaRepository parkingAreaRepository,
                              ParkingSlotRepository parkingSlotRepository,
                              AuditLogService auditLogService) {
        this.parkingAreaRepository = parkingAreaRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ParkingAreaDto> getAllParkingAreas() {
        return parkingAreaRepository.findAll().stream()
                .map(this::enrichAreaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingAreaDto> getActiveParkingAreas() {
        return parkingAreaRepository.findByActiveTrue().stream()
                .map(this::enrichAreaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingArea getParkingAreaEntityById(Long id) {
        return parkingAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Area not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ParkingAreaDto getParkingAreaById(Long id) {
        return enrichAreaDto(getParkingAreaEntityById(id));
    }

    @Transactional
    public ParkingAreaDto createParkingArea(ParkingAreaDto dto) {
        ParkingArea area = new ParkingArea();
        area.setName(dto.getName());
        area.setAddress(dto.getAddress());
        area.setDescription(dto.getDescription());
        area.setOpeningTime(dto.getOpeningTime());
        area.setClosingTime(dto.getClosingTime());
        area.setActive(dto.isActive());

        ParkingArea saved = parkingAreaRepository.save(area);
        auditLogService.log(null, "CREATE_PARKING_AREA", "Created parking area: " + saved.getName());
        return enrichAreaDto(saved);
    }

    @Transactional
    public ParkingAreaDto updateParkingArea(Long id, ParkingAreaDto dto) {
        ParkingArea area = getParkingAreaEntityById(id);
        area.setName(dto.getName());
        area.setAddress(dto.getAddress());
        area.setDescription(dto.getDescription());
        area.setOpeningTime(dto.getOpeningTime());
        area.setClosingTime(dto.getClosingTime());
        area.setActive(dto.isActive());

        ParkingArea saved = parkingAreaRepository.save(area);
        auditLogService.log(null, "UPDATE_PARKING_AREA", "Updated parking area id: " + id);
        return enrichAreaDto(saved);
    }

    @Transactional
    public void deleteParkingArea(Long id) {
        ParkingArea area = getParkingAreaEntityById(id);
        parkingAreaRepository.delete(area);
        auditLogService.log(null, "DELETE_PARKING_AREA", "Deleted parking area id: " + id);
    }

    private ParkingAreaDto enrichAreaDto(ParkingArea area) {
        ParkingAreaDto dto = ParkingAreaDto.fromEntity(area);
        int total = area.getSlots() != null ? area.getSlots().size() : 0;
        int available = (int) (area.getSlots() != null ? area.getSlots().stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .count() : 0);
        dto.setTotalSlots(total);
        dto.setAvailableSlots(available);
        return dto;
    }
}
