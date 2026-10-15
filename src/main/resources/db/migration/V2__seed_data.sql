-- ===================================================================
-- SMART PARK Realistic Development Seed Data
-- ===================================================================

-- Passwords below are BCrypt encoded for:
-- admin@smartpark.com    -> Admin@123
-- staff@smartpark.com    -> Staff@123
-- customer@smartpark.com -> Customer@123

INSERT INTO users (id, full_name, email, password_hash, phone, role, enabled, created_at)
VALUES 
(1, 'System Administrator', 'admin@smartpark.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '+1-555-0100', 'ADMIN', TRUE, NOW()),
(2, 'Marcus Vance (Staff)', 'staff@smartpark.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '+1-555-0101', 'STAFF', TRUE, NOW()),
(3, 'Alex Johnson', 'customer@smartpark.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '+1-555-0102', 'CUSTOMER', TRUE, NOW()),
(4, 'Sarah Miller', 'sarah.miller@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '+1-555-0103', 'CUSTOMER', TRUE, NOW());

-- Parking Areas
INSERT INTO parking_areas (id, name, address, description, opening_time, closing_time, active)
VALUES
(1, 'Metro Central Plaza', '100 Financial Way, Downtown', 'Multi-level premium parking facility with fast EV charging stations and 24/7 security.', '06:00:00', '23:30:00', TRUE),
(2, 'Grand North Terminal', '45 Airport Boulevard, North Wing', 'Spacious open-air and covered slots near transit connection points and express shuttle.', '05:00:00', '23:00:00', TRUE),
(3, 'Harbor View Deck', '78 Waterfront Promenade', 'Scenic waterfront parking structure serving commercial piers, retail, and dining venues.', '07:00:00', '22:00:00', TRUE);

-- Parking Slots for Metro Central Plaza (Area 1)
INSERT INTO parking_slots (id, parking_area_id, slot_number, vehicle_type, hourly_rate, status, created_at)
VALUES
(1, 1, 'A-101', 'CAR', 5.00, 'AVAILABLE', NOW()),
(2, 1, 'A-102', 'CAR', 5.00, 'AVAILABLE', NOW()),
(3, 1, 'A-103', 'CAR', 5.00, 'RESERVED', NOW()),
(4, 1, 'A-104', 'EV', 7.50, 'AVAILABLE', NOW()),
(5, 1, 'A-105', 'EV', 7.50, 'OCCUPIED', NOW()),
(6, 1, 'B-201', 'MOTORCYCLE', 2.50, 'AVAILABLE', NOW()),
(7, 1, 'B-202', 'MOTORCYCLE', 2.50, 'AVAILABLE', NOW()),
(8, 1, 'C-301', 'TRUCK', 12.00, 'AVAILABLE', NOW()),
(9, 1, 'C-302', 'TRUCK', 12.00, 'INACTIVE', NOW());

-- Parking Slots for Grand North Terminal (Area 2)
INSERT INTO parking_slots (id, parking_area_id, slot_number, vehicle_type, hourly_rate, status, created_at)
VALUES
(10, 2, 'N-01', 'CAR', 4.00, 'AVAILABLE', NOW()),
(11, 2, 'N-02', 'CAR', 4.00, 'AVAILABLE', NOW()),
(12, 2, 'N-03', 'CAR', 4.00, 'AVAILABLE', NOW()),
(13, 2, 'N-04', 'EV', 6.00, 'AVAILABLE', NOW()),
(14, 2, 'N-05', 'MOTORCYCLE', 2.00, 'AVAILABLE', NOW()),
(15, 2, 'N-06', 'TRUCK', 10.00, 'AVAILABLE', NOW());

-- Parking Slots for Harbor View Deck (Area 3)
INSERT INTO parking_slots (id, parking_area_id, slot_number, vehicle_type, hourly_rate, status, created_at)
VALUES
(16, 3, 'H-01', 'CAR', 4.50, 'AVAILABLE', NOW()),
(17, 3, 'H-02', 'CAR', 4.50, 'AVAILABLE', NOW()),
(18, 3, 'H-03', 'EV', 6.50, 'AVAILABLE', NOW());

-- Customer Vehicles
INSERT INTO vehicles (id, user_id, registration_number, vehicle_type, created_at)
VALUES
(1, 3, 'CA-789-XYZ', 'CAR', NOW()),
(2, 3, 'EV-345-TES', 'EV', NOW()),
(3, 4, 'NY-123-ABC', 'CAR', NOW()),
(4, 4, 'MC-888-MOTO', 'MOTORCYCLE', NOW());

-- Staff Assignments
INSERT INTO staff_assignments (id, staff_user_id, parking_area_id)
VALUES
(1, 2, 1),
(2, 2, 2);

-- Sample Reservations
INSERT INTO reservations (id, booking_reference, user_id, vehicle_id, parking_slot_id, start_time, end_time, status, qr_token, created_at)
VALUES
(1, 'SP-RES-2026-001', 3, 1, 3, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'CONFIRMED', 'qr-token-uuid-001-abc', NOW()),
(2, 'SP-RES-2026-002', 3, 2, 5, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'ACTIVE', 'qr-token-uuid-002-def', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'SP-RES-2026-003', 4, 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 20 HOUR), 'COMPLETED', 'qr-token-uuid-003-ghi', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Parking Session for Active and Completed Reservations
INSERT INTO parking_sessions (id, reservation_id, entry_time, exit_time, duration_minutes, calculated_fee, created_at)
VALUES
(1, 2, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 20 HOUR), 240, 20.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Payments
INSERT INTO payments (id, reservation_id, amount, payment_method, transaction_reference, payment_status, paid_at, created_at)
VALUES
(1, 1, 15.00, 'CREDIT_CARD', 'TXN-SP-2026-8801', 'COMPLETED', NOW(), NOW()),
(2, 2, 22.50, 'DEMO_PAYMENT', 'TXN-SP-2026-8802', 'PENDING', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 3, 20.00, 'DEBIT_CARD', 'TXN-SP-2026-8803', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- Audit Logs
INSERT INTO audit_logs (id, user_id, action, details, created_at)
VALUES
(1, 1, 'SYSTEM_INIT', 'Smart Park initialized with standard layout and seed data.', NOW()),
(2, 3, 'CREATE_RESERVATION', 'Reservation SP-RES-2026-001 created for slot A-103.', NOW()),
(3, 2, 'RECORD_ENTRY', 'Staff Marcus Vance checked in reservation SP-RES-2026-002.', DATE_SUB(NOW(), INTERVAL 1 HOUR));
