package com.smartpark.config;

import com.smartpark.entity.*;
import com.smartpark.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ParkingAreaRepository parkingAreaRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ParkingAreaRepository parkingAreaRepository,
                           ParkingSlotRepository parkingSlotRepository,
                           VehicleRepository vehicleRepository,
                           StaffAssignmentRepository staffAssignmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.parkingAreaRepository = parkingAreaRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.vehicleRepository = vehicleRepository;
        this.staffAssignmentRepository = staffAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking SMART PARK seed data initialization...");

        // 1. Seed Admin
        User admin = userRepository.findByEmail("admin@smartpark.com").orElseGet(() -> {
            User u = new User();
            u.setFullName("System Administrator");
            u.setEmail("admin@smartpark.com");
            u.setPasswordHash(passwordEncoder.encode("Admin@123"));
            u.setPhone("+1-555-0100");
            u.setRole(Role.ADMIN);
            u.setEnabled(true);
            return userRepository.save(u);
        });

        // 2. Seed Staff
        User staff = userRepository.findByEmail("staff@smartpark.com").orElseGet(() -> {
            User u = new User();
            u.setFullName("Marcus Vance (Staff)");
            u.setEmail("staff@smartpark.com");
            u.setPasswordHash(passwordEncoder.encode("Staff@123"));
            u.setPhone("+1-555-0101");
            u.setRole(Role.STAFF);
            u.setEnabled(true);
            return userRepository.save(u);
        });

        // 3. Seed Customer
        User customer = userRepository.findByEmail("customer@smartpark.com").orElseGet(() -> {
            User u = new User();
            u.setFullName("Alex Johnson");
            u.setEmail("customer@smartpark.com");
            u.setPasswordHash(passwordEncoder.encode("Customer@123"));
            u.setPhone("+1-555-0102");
            u.setRole(Role.CUSTOMER);
            u.setEnabled(true);
            return userRepository.save(u);
        });

        // 4. Seed Parking Areas & Slots if empty
        if (parkingAreaRepository.count() == 0) {
            ParkingArea area1 = new ParkingArea(
                    "Metro Central Plaza",
                    "100 Financial Way, Downtown",
                    "Multi-level premium parking facility with fast EV charging stations and 24/7 security.",
                    LocalTime.of(6, 0),
                    LocalTime.of(23, 30),
                    true
            );
            parkingAreaRepository.save(area1);

            ParkingArea area2 = new ParkingArea(
                    "Grand North Terminal",
                    "45 Airport Boulevard, North Wing",
                    "Spacious open-air and covered slots near transit connection points and express shuttle.",
                    LocalTime.of(5, 0),
                    LocalTime.of(23, 0),
                    true
            );
            parkingAreaRepository.save(area2);

            ParkingArea area3 = new ParkingArea(
                    "Harbor View Deck",
                    "78 Waterfront Promenade",
                    "Scenic waterfront parking structure serving commercial piers, retail, and dining venues.",
                    LocalTime.of(7, 0),
                    LocalTime.of(22, 0),
                    true
            );
            parkingAreaRepository.save(area3);

            // Area 1 Slots
            parkingSlotRepository.save(new ParkingSlot(area1, "A-101", VehicleType.CAR, new BigDecimal("5.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area1, "A-102", VehicleType.CAR, new BigDecimal("5.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area1, "A-103", VehicleType.CAR, new BigDecimal("5.00"), SlotStatus.RESERVED));
            parkingSlotRepository.save(new ParkingSlot(area1, "A-104", VehicleType.EV, new BigDecimal("7.50"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area1, "A-105", VehicleType.EV, new BigDecimal("7.50"), SlotStatus.OCCUPIED));
            parkingSlotRepository.save(new ParkingSlot(area1, "B-201", VehicleType.MOTORCYCLE, new BigDecimal("2.50"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area1, "B-202", VehicleType.MOTORCYCLE, new BigDecimal("2.50"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area1, "C-301", VehicleType.TRUCK, new BigDecimal("12.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area1, "C-302", VehicleType.TRUCK, new BigDecimal("12.00"), SlotStatus.INACTIVE));

            // Area 2 Slots
            parkingSlotRepository.save(new ParkingSlot(area2, "N-01", VehicleType.CAR, new BigDecimal("4.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area2, "N-02", VehicleType.CAR, new BigDecimal("4.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area2, "N-03", VehicleType.CAR, new BigDecimal("4.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area2, "N-04", VehicleType.EV, new BigDecimal("6.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area2, "N-05", VehicleType.MOTORCYCLE, new BigDecimal("2.00"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area2, "N-06", VehicleType.TRUCK, new BigDecimal("10.00"), SlotStatus.AVAILABLE));

            // Area 3 Slots
            parkingSlotRepository.save(new ParkingSlot(area3, "H-01", VehicleType.CAR, new BigDecimal("4.50"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area3, "H-02", VehicleType.CAR, new BigDecimal("4.50"), SlotStatus.AVAILABLE));
            parkingSlotRepository.save(new ParkingSlot(area3, "H-03", VehicleType.EV, new BigDecimal("6.50"), SlotStatus.AVAILABLE));

            // Staff Assignments
            staffAssignmentRepository.save(new StaffAssignment(staff, area1));
            staffAssignmentRepository.save(new StaffAssignment(staff, area2));
        }

        // 5. Seed Customer Vehicle if none exists
        if (vehicleRepository.findByUserId(customer.getId()).isEmpty()) {
            vehicleRepository.save(new Vehicle(customer, "CA-789-XYZ", VehicleType.CAR));
            vehicleRepository.save(new Vehicle(customer, "EV-345-TES", VehicleType.EV));
        }

        log.info("SMART PARK seed data initialization complete.");
    }
}
