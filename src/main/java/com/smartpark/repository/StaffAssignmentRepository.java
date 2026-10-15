package com.smartpark.repository;

import com.smartpark.entity.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
    List<StaffAssignment> findByStaffUserId(Long staffUserId);
    List<StaffAssignment> findByParkingAreaId(Long parkingAreaId);
    boolean existsByStaffUserIdAndParkingAreaId(Long staffUserId, Long parkingAreaId);
    Optional<StaffAssignment> findByStaffUserIdAndParkingAreaId(Long staffUserId, Long parkingAreaId);
    void deleteByStaffUserIdAndParkingAreaId(Long staffUserId, Long parkingAreaId);
}
