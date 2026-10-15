package com.smartpark.service;

import com.smartpark.entity.ParkingArea;
import com.smartpark.entity.Role;
import com.smartpark.entity.StaffAssignment;
import com.smartpark.entity.User;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingAreaRepository;
import com.smartpark.repository.StaffAssignmentRepository;
import com.smartpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffAssignmentService {

    private final StaffAssignmentRepository staffAssignmentRepository;
    private final UserRepository userRepository;
    private final ParkingAreaRepository parkingAreaRepository;
    private final AuditLogService auditLogService;

    public StaffAssignmentService(StaffAssignmentRepository staffAssignmentRepository,
                                  UserRepository userRepository,
                                  ParkingAreaRepository parkingAreaRepository,
                                  AuditLogService auditLogService) {
        this.staffAssignmentRepository = staffAssignmentRepository;
        this.userRepository = userRepository;
        this.parkingAreaRepository = parkingAreaRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void assignStaff(Long staffUserId, Long parkingAreaId) {
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with id: " + staffUserId));

        if (staff.getRole() != Role.STAFF && staff.getRole() != Role.ADMIN) {
            throw new BadRequestException("User " + staff.getEmail() + " is not a STAFF member.");
        }

        ParkingArea area = parkingAreaRepository.findById(parkingAreaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking area not found with id: " + parkingAreaId));

        if (staffAssignmentRepository.existsByStaffUserIdAndParkingAreaId(staffUserId, parkingAreaId)) {
            return; // Already assigned
        }

        StaffAssignment assignment = new StaffAssignment(staff, area);
        staffAssignmentRepository.save(assignment);
        auditLogService.log(staff, "ASSIGN_STAFF", "Assigned staff member to parking area: " + area.getName());
    }

    @Transactional
    public void removeAssignment(Long staffUserId, Long parkingAreaId) {
        staffAssignmentRepository.deleteByStaffUserIdAndParkingAreaId(staffUserId, parkingAreaId);
        auditLogService.log(staffUserId, "UNASSIGN_STAFF", "Unassigned staff member from parking area id: " + parkingAreaId);
    }

    @Transactional(readOnly = true)
    public List<Long> getAssignedAreaIds(Long staffUserId) {
        return staffAssignmentRepository.findByStaffUserId(staffUserId).stream()
                .map(a -> a.getParkingArea().getId())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingArea> getAssignedAreas(Long staffUserId) {
        return staffAssignmentRepository.findByStaffUserId(staffUserId).stream()
                .map(StaffAssignment::getParkingArea)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isAuthorized(User user, Long parkingAreaId) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.STAFF) {
            return staffAssignmentRepository.existsByStaffUserIdAndParkingAreaId(user.getId(), parkingAreaId);
        }
        return false;
    }
}
