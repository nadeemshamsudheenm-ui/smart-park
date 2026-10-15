package com.smartpark.controller;

import com.smartpark.dto.ApiResponse;
import com.smartpark.dto.ParkingSlotDto;
import com.smartpark.entity.VehicleType;
import com.smartpark.service.ParkingSlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/parking-slots")
public class ParkingSlotRestController {

    private final ParkingSlotService parkingSlotService;

    public ParkingSlotRestController(ParkingSlotService parkingSlotService) {
        this.parkingSlotService = parkingSlotService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSlotDto>>> getAllSlots() {
        return ResponseEntity.ok(ApiResponse.success("Slots retrieved", parkingSlotService.getAllSlots()));
    }

    @GetMapping("/area/{areaId}")
    public ResponseEntity<ApiResponse<List<ParkingSlotDto>>> getSlotsByArea(@PathVariable Long areaId) {
        return ResponseEntity.ok(ApiResponse.success("Area slots retrieved", parkingSlotService.getSlotsByArea(areaId)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ParkingSlotDto>>> getAvailableSlots(
            @RequestParam Long areaId,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now();
        LocalDateTime end = endTime != null ? endTime : start.plusHours(2);

        List<ParkingSlotDto> availableSlots = parkingSlotService.getAvailableSlotsForInterval(areaId, vehicleType, start, end);
        return ResponseEntity.ok(ApiResponse.success("Available slots for interval", availableSlots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSlotDto>> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Slot details", parkingSlotService.getSlotById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingSlotDto>> createSlot(@Valid @RequestBody ParkingSlotDto dto) {
        ParkingSlotDto created = parkingSlotService.createSlot(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parking slot created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingSlotDto>> updateSlot(@PathVariable Long id, @Valid @RequestBody ParkingSlotDto dto) {
        ParkingSlotDto updated = parkingSlotService.updateSlot(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Parking slot updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable Long id) {
        parkingSlotService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Parking slot deleted successfully"));
    }
}
