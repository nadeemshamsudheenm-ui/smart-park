# SMART PARK — An Object-Oriented Digital Parking Platform with Real-Time Slot Management

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Security](https://img.shields.io/badge/Spring%20Security-6.x-red.svg)](https://spring.io/projects/spring-security)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple.svg)](https://getbootstrap.com/)

---

## 1. Project Overview

**SMART PARK** is an enterprise-grade, object-oriented digital parking management web platform. Designed as a robust three-tier system, SMART PARK solves urban parking congestion through real-time slot state management, mathematical interval double-booking prevention, contactless ZXing QR code passes, and automated billing calculation upon vehicle checkout.

### Key Capabilities
- **Real-Time Interactive Slot Engine**: Visual parking grid with color-coded states:
  - 🟢 **AVAILABLE**: Ready for immediate or future booking
  - 🔴 **OCCUPIED**: Vehicle currently parked inside the slot
  - 🟠 **RESERVED**: Scheduled for an overlapping reservation window
  - ⚪ **INACTIVE**: Temporarily closed for maintenance or repairs
- **Conflict-Free Interval Engine**: Transactional mathematical overlap validation (`:startTime < r.endTime AND :endTime > r.startTime`) prevents double-bookings at the database layer.
- **Cryptographic QR Passes**: High-security UUID tokens encoded into base64 QR codes using ZXing, verified at entry gates by authorized facility staff.
- **Automated Duration & Billing Engine**: Timestamp tracking with configurable minimum-hour policies, rounding up to whole hours, and instant demo payment settlements.
- **Role-Based Portals**: Tailored interfaces and server-side authorization for **Customers**, **Staff**, and **Administrators**.
- **Live Analytics**: Chart.js data visualizations for administrative capacity tracking and daily revenue trends.

---

## 2. Technology Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6 with BCrypt password hashing & session authentication
- **Data Persistence**: Spring Data JPA & Hibernate ORM
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`)
- **QR Engine**: ZXing (`core` & `javase` 3.5.3)
- **Build Tool**: Apache Maven

### Database
- **Primary Database**: MySQL 8.x (`smartpark_db`)
- **Driver**: MySQL Connector/J (`com.mysql.cj.jdbc.Driver`)
- **Schema Management**: Flyway / SQL versioned migration scripts (`V1__init_schema.sql`, `V2__seed_data.sql`)
- **Test In-Memory Database**: H2 Database (with MySQL dialect mode for fast test execution)

### Frontend
- **Templating**: Thymeleaf 3 with Spring Security 6 tags
- **UI Framework**: Bootstrap 5.3 & Bootstrap Icons & FontAwesome 6
- **Data Visualizations**: Chart.js 4.4
- **Asynchronous Communication**: Native JavaScript Fetch API

---

## 3. Architecture & Data Flow

SMART PARK enforces a strict three-tier architecture:

```
[ User Browser / Device ]
           │
     (REST / HTML)
           ▼
[ Spring Boot Controller Layer ]
  - ViewController (Thymeleaf Routes)
  - REST Controllers (JSON Endpoints)
           │
           ▼
[ Business & Service Layer ]
  - ReservationService (Double-booking lock & interval algorithms)
  - ParkingSessionService (Gate check-in / check-out)
  - BillingService (Duration & rounding calculations)
  - QrCodeService (ZXing byte matrix generation)
  - UserService & AuditLogService
           │
           ▼
[ Data Access / JPA Repository Layer ]
  - Relational Queries & Transaction Boundaries
           │
           ▼
[ MySQL 8 Database (smartpark_db) ]
  - users, parking_areas, parking_slots, vehicles,
    reservations, parking_sessions, payments, staff_assignments, audit_logs
```

---

## 4. Database Setup & Initialization

### Step 1: Create Database in MySQL
Log into MySQL Server using MySQL Workbench or the `mysql` CLI:

```sql
-- Connect to MySQL as root or privileged administrator
mysql -u root -p

-- Create database
CREATE DATABASE IF NOT EXISTS smartpark_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- (Optional) Create a dedicated user for Smart Park
CREATE USER IF NOT EXISTS 'smartpark_user'@'localhost' IDENTIFIED BY 'SmartPark@Pass2026';
GRANT ALL PRIVILEGES ON smartpark_db.* TO 'smartpark_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2: Initialize Tables and Seed Data
You can either run the migration scripts directly in MySQL or let Spring Boot automatically initialize them on startup:

```bash
# Execute schema definition
mysql -u root -p smartpark_db < src/main/resources/db/migration/V1__init_schema.sql

# Execute development seed data
mysql -u root -p smartpark_db < src/main/resources/db/migration/V2__seed_data.sql
```

---

## 5. Configuration & Environment Variables

Database connectivity is configured in `src/main/resources/application.properties` using standard environment variables with convenient defaults:

| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| `DB_URL` | JDBC Connection URL | `jdbc:mysql://localhost:3306/smartpark_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL Username | `root` |
| `DB_PASSWORD` | MySQL Password | *(empty or your custom password)* |

### Example `application.properties`:
```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/smartpark_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
smartpark.billing.minimum-charge-hours=1
```

To run with custom credentials in Windows PowerShell:
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
mvn spring-boot:run
```

---

## 6. Default Demo Credentials

The database seed script and `DataInitializer` pre-populate default accounts for testing:

| Role | Email | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@smartpark.com` | `Admin@123` | Full administrative control, facility CRUD, slot pricing, staff assignment, financial reports, audit trail. |
| **STAFF** | `staff@smartpark.com` | `Staff@123` | Gate scanner, QR token verification, vehicle check-in, check-out duration & billing calculation for assigned facilities. |
| **CUSTOMER** | `customer@smartpark.com` | `Customer@123` | Vehicle registration, real-time slot selection, booking confirmations, QR pass generator, printable receipts, payments. |

*Note: Public registration at `/register` creates **CUSTOMER** accounts only. Admin and Staff accounts can never be registered publicly.*

---

## 7. REST API Endpoints

### Authentication
- `POST /api/auth/register` — Public customer registration
- `GET /api/auth/current-user` — Authenticated user profile

### Parking Areas & Slots
- `GET /api/parking-areas` — List active facilities
- `GET /api/parking-areas/{id}` — Get single facility details
- `POST /api/admin/parking-areas` — *[ADMIN]* Create facility
- `PUT /api/admin/parking-areas/{id}` — *[ADMIN]* Edit facility
- `DELETE /api/admin/parking-areas/{id}` — *[ADMIN]* Delete facility
- `GET /api/parking-slots/available` — Query slots available for `[startTime, endTime]` and vehicle type
- `GET /api/parking-slots/area/{areaId}` — Get all slots for an area
- `POST /api/parking-slots` — *[ADMIN]* Create slot
- `PUT /api/parking-slots/{id}` — *[ADMIN]* Update slot rate/type/status

### Vehicles
- `GET /api/vehicles` — List logged-in customer's vehicles
- `POST /api/vehicles` — Register a vehicle license plate
- `DELETE /api/vehicles/{id}` — Remove a vehicle

### Reservations
- `POST /api/reservations` — Create reservation (transactional double-booking lock)
- `GET /api/reservations/my` — List customer reservations
- `GET /api/reservations/{id}` — View reservation details
- `POST /api/reservations/{id}/cancel` — Cancel eligible reservation & release slot

### Staff Gate Operations
- `GET /api/staff/dashboard-stats` — Live stats for assigned areas
- `POST /api/staff/verify-booking` — Validate reference or QR token
- `POST /api/staff/sessions/check-in` — Record vehicle entry, mark slot OCCUPIED
- `POST /api/staff/sessions/check-out` — Record exit, calculate fee, mark slot AVAILABLE
- `GET /api/staff/sessions/active` — List currently parked vehicles

### Payments
- `POST /api/payments/demo` — Process simulated instant payment
- `GET /api/payments/my` — Customer transaction history
- `GET /api/payments` — *[ADMIN]* Master transaction ledger

### Admin Dashboard & Reports
- `GET /api/admin/dashboard` — Capacity, session, and revenue summary metrics
- `GET /api/admin/reports/occupancy` — Capacity by facility for Chart.js
- `GET /api/admin/reports/revenue` — Last 7 days revenue trend for Chart.js
- `GET /api/admin/users` — List all registered users
- `PUT /api/admin/users/{id}/status` — Enable / disable account
- `PUT /api/admin/users/{id}/role` — Change user role
- `POST /api/admin/staff-assignments` — Assign staff member to facility
- `GET /api/admin/audit-logs` — Administrative audit log records

---

## 8. Building and Testing

### Run Automated Tests
The automated test suite runs against an isolated, high-speed H2 in-memory MySQL mode that requires no external database credentials:
```bash
mvn clean test
```

### Start Application
```bash
mvn spring-boot:run
```
Once started, access the application in your browser:
**`http://localhost:8080`**
