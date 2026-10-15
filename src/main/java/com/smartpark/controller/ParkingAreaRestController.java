package com.smartpark.controller;

import com.smartpark.dto.ApiResponse;
import com.smartpark.dto.ParkingAreaDto;
import com.smartpark.service.ParkingAreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-areas")
public class ParkingAreaRestController {

    private final ParkingAreaService parkingAreaService;

    public ParkingAreaRestController(ParkingAreaService parkingAreaService) {
        this.parkingAreaService = parkingAreaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingAreaDto>>> getAllAreas() {
        return ResponseEntity.ok(ApiResponse.success("Parking areas retrieved", parkingAreaService.getActiveParkingAreas()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ParkingAreaDto>>> getAllAreasIncludingInactive() {
        return ResponseEntity.ok(ApiResponse.success("All parking areas retrieved", parkingAreaService.getAllParkingAreas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingAreaDto>> getAreaById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Parking area details", parkingAreaService.getParkingAreaById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingAreaDto>> createArea(@Valid @RequestBody ParkingAreaDto dto) {
        ParkingAreaDto created = parkingAreaService.createParkingArea(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parking area created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingAreaDto>> updateArea(@PathVariable Long id, @Valid @RequestBody ParkingAreaDto dto) {
        ParkingAreaDto updated = parkingAreaService.updateParkingArea(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Parking area updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArea(@PathVariable Long id) {
        parkingAreaService.deleteParkingArea(id);
        return ResponseEntity.ok(ApiResponse.success("Parking area deleted successfully"));
    }
}
