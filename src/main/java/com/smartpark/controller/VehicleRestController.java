package com.smartpark.controller;

import com.smartpark.dto.ApiResponse;
import com.smartpark.dto.VehicleDto;
import com.smartpark.security.CustomUserDetails;
import com.smartpark.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleRestController {

    private final VehicleService vehicleService;

    public VehicleRestController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getMyVehicles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<VehicleDto> vehicles = vehicleService.getVehiclesByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("User vehicles retrieved", vehicles));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleDto>> registerVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VehicleDto dto) {
        VehicleDto registered = vehicleService.registerVehicle(userDetails.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered successfully", registered));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        vehicleService.deleteVehicle(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully"));
    }
}
