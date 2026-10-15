package com.smartpark.service;

import com.smartpark.dto.VehicleDto;
import com.smartpark.entity.User;
import com.smartpark.entity.Vehicle;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public VehicleService(VehicleRepository vehicleRepository, UserService userService, AuditLogService auditLogService) {
        this.vehicleRepository = vehicleRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId).stream()
                .map(VehicleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleDto registerVehicle(Long userId, VehicleDto dto) {
        String regNum = dto.getRegistrationNumber().trim().toUpperCase();
        if (vehicleRepository.existsByRegistrationNumber(regNum)) {
            throw new BadRequestException("A vehicle with registration number " + regNum + " is already registered.");
        }

        User user = userService.getUserById(userId);
        Vehicle vehicle = new Vehicle(user, regNum, dto.getVehicleType());
        Vehicle saved = vehicleRepository.save(vehicle);

        auditLogService.log(user, "REGISTER_VEHICLE", "Registered vehicle " + regNum + " (" + dto.getVehicleType() + ")");
        return VehicleDto.fromEntity(saved);
    }

    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not own this vehicle.");
        }

        vehicleRepository.delete(vehicle);
        auditLogService.log(userId, "DELETE_VEHICLE", "Removed vehicle: " + vehicle.getRegistrationNumber());
    }
}
