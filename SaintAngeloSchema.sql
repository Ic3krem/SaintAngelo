-- =====================================================
-- SaintAngelo Hospital Queue Management System
-- MySQL Database Schema
-- =====================================================
-- Database: saintangelo_hospital
-- Version: 1.0
-- Created for: XAMPP MySQL
-- =====================================================

-- Drop database if exists (use with caution in production)
-- DROP DATABASE IF EXISTS saintangelo_hospital;

-- Create database
CREATE DATABASE IF NOT EXISTS saintangelo_hospital
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE saintangelo_hospital;

-- =====================================================
-- 1. USERS TABLE
-- Stores all system users (Admin, Doctor, Receptionist)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'Hashed password',
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('SUPER_ADMIN', 'ADMIN', 'DOCTOR', 'STAFF') NOT NULL DEFAULT 'STAFF',
    permissions VARCHAR(255) DEFAULT NULL COMMENT 'Permissions description',
    status ENUM('Active', 'Inactive') NOT NULL DEFAULT 'Active',
    last_active DATETIME DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. DOCTORS TABLE
-- Stores doctor-specific information
-- =====================================================
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id VARCHAR(20) PRIMARY KEY,
    user_id VARCHAR(20) DEFAULT NULL COMMENT 'Reference to users table',
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    specialization VARCHAR(100) DEFAULT NULL,
    department VARCHAR(100) DEFAULT NULL,
    building_number INT DEFAULT NULL,
    status ENUM('Active', 'Inactive') NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_email (email),
    INDEX idx_department (department),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. PATIENTS TABLE
-- Stores patient information
-- =====================================================
CREATE TABLE IF NOT EXISTS patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    gender ENUM('Male', 'Female', 'Other') DEFAULT NULL,
    home_address TEXT DEFAULT NULL,
    chief_complaint TEXT DEFAULT NULL COMMENT 'Reason for visit',
    priority ENUM('REGULAR', 'SENIOR_CITIZEN', 'EMERGENCY') NOT NULL DEFAULT 'REGULAR',
    emergency_contact_person VARCHAR(100) DEFAULT NULL,
    emergency_contact_number VARCHAR(20) DEFAULT NULL,
    is_senior_citizen BOOLEAN DEFAULT FALSE,
    blood_type VARCHAR(10) DEFAULT NULL,
    registration_date DATE NOT NULL,
    last_visit_date DATE DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (first_name, last_name),
    INDEX idx_phone (phone_number),
    INDEX idx_priority (priority),
    INDEX idx_registration_date (registration_date),
    INDEX idx_last_visit_date (last_visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. TICKETS TABLE
-- Stores queue tickets for patient visits
-- =====================================================
CREATE TABLE IF NOT EXISTS tickets (
    visit_id VARCHAR(20) PRIMARY KEY,
    ticket_number VARCHAR(10) NOT NULL COMMENT 'Formatted like A1, B5, etc.',
    patient_id VARCHAR(20) NOT NULL,
    status ENUM('WAITING', 'CALLED', 'IN_SERVICE', 'COMPLETED', 'SKIPPED') NOT NULL DEFAULT 'WAITING',
    priority ENUM('REGULAR', 'SENIOR_CITIZEN', 'EMERGENCY') NOT NULL DEFAULT 'REGULAR',
    service_type VARCHAR(100) DEFAULT NULL,
    assigned_doctor_id VARCHAR(20) DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_time DATETIME DEFAULT NULL,
    completed_time DATETIME DEFAULT NULL,
    wait_time_minutes INT DEFAULT 0 COMMENT 'Calculated wait time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_ticket_number (ticket_number),
    INDEX idx_patient_id (patient_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_assigned_doctor (assigned_doctor_id),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. APPOINTMENTS TABLE
-- Stores scheduled appointments
-- =====================================================
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    purpose TEXT DEFAULT NULL COMMENT 'Reason for appointment',
    status ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_status (status),
    INDEX idx_date_time (appointment_date, appointment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. PRESCRIPTIONS TABLE
-- Stores prescription and consultation records
-- =====================================================
CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    medication VARCHAR(255) NOT NULL,
    dosage VARCHAR(100) DEFAULT NULL COMMENT 'e.g., 500 mg',
    frequency VARCHAR(100) DEFAULT NULL COMMENT 'e.g., 3x daily',
    consultation_notes TEXT DEFAULT NULL,
    diagnosis TEXT DEFAULT NULL,
    treatment_plan TEXT DEFAULT NULL,
    consultation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_consultation_date (consultation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. DISCHARGES TABLE
-- Stores patient discharge records
-- =====================================================
CREATE TABLE IF NOT EXISTS discharges (
    discharge_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    department VARCHAR(100) NOT NULL,
    status ENUM('READY', 'PENDING_REVIEW', 'CLEARED', 'DISCHARGED') NOT NULL DEFAULT 'PENDING_REVIEW',
    prescription_id VARCHAR(20) DEFAULT NULL,
    discharge_date DATETIME DEFAULT NULL,
    billing_amount DECIMAL(10, 2) DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_patient_id (patient_id),
    INDEX idx_status (status),
    INDEX idx_department (department),
    INDEX idx_discharge_date (discharge_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. ACTIVITY_LOGS TABLE
-- Stores system activity logs for audit trail
-- =====================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) DEFAULT NULL,
    action VARCHAR(100) NOT NULL COMMENT 'e.g., Login, Create Patient, Update Queue',
    details TEXT DEFAULT NULL,
    activity_type ENUM('LOGIN', 'PATIENT', 'QUEUE', 'APPOINTMENT', 'DISCHARGE', 'USER_MANAGEMENT', 'REPORT') NOT NULL,
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IPv4 or IPv6 address',
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_activity_type (activity_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. REPORTS TABLE
-- Stores generated reports metadata
-- =====================================================
CREATE TABLE IF NOT EXISTS reports (
    report_id VARCHAR(20) PRIMARY KEY,
    report_type ENUM('DAILY_STATISTICS', 'WEEKLY_STATISTICS', 'MONTHLY_STATISTICS', 'CUSTOM') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    report_format ENUM('PDF', 'EXCEL', 'CSV') NOT NULL DEFAULT 'PDF',
    generated_by VARCHAR(20) NOT NULL,
    file_path VARCHAR(500) DEFAULT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (generated_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_report_type (report_type),
    INDEX idx_generated_by (generated_by),
    INDEX idx_generated_at (generated_at),
    INDEX idx_date_range (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Insert sample users (plain text passwords for development)
INSERT INTO users (user_id, username, password, full_name, email, role, permissions, status) VALUES
('U001', 'admin', 'password', 'Admin User', 'admin@stangelo.com', 'ADMIN', 'System Configuration', 'Active'),
('U002', 'doctor', 'password', 'Dr. John Diaz', 'john.diaz@stangelo.com', 'DOCTOR', 'Full Medical Access', 'Active'),
('U003', 'reception', 'password', 'Angelo Castro', 'angelo.castro@stangelo.com', 'STAFF', 'Registration & Queue', 'Active'),
('U004', 'sarah.lee', 'password', 'Dr. Sarah Lee', 'sarah.lee@stangelo.com', 'DOCTOR', 'Full Medical Access', 'Active'),
('U005', 'maria.santos', 'password', 'Maria Santos', 'maria.santos@stangelo.com', 'STAFF', 'Registration & Queue', 'Active');

-- Insert sample doctors
INSERT INTO doctors (doctor_id, user_id, name, email, specialization, department, building_number, status) VALUES
('DOC001', 'U002', 'Dr. John Diaz', 'john.diaz@stangelo.com', 'General Medicine', 'General', 1, 'Active'),
('DOC002', 'U004', 'Dr. Sarah Lee', 'sarah.lee@stangelo.com', 'Cardiology', 'Cardiology', 2, 'Active');

-- Insert sample patients
INSERT INTO patients (patient_id, first_name, last_name, age, phone_number, gender, home_address, chief_complaint, priority, emergency_contact_person, emergency_contact_number, is_senior_citizen, blood_type, registration_date, last_visit_date) VALUES
('A145', 'Patrick', 'Claridad', 51, '0912-345-6789', 'Male', '123 Main St, City', 'Backburner', 'REGULAR', 'Jane Claridad', '0912-345-6790', FALSE, 'O+', '2025-01-15', '2025-11-28'),
('A144', 'Maria', 'Santos', 45, '0923-456-7890', 'Female', '456 Oak Ave, City', 'Chest Pain', 'EMERGENCY', 'Juan Santos', '0923-456-7891', FALSE, 'A+', '2025-01-10', '2025-11-28'),
('A143', 'Juan', 'Dela Cruz', 45, '0934-567-8901', 'Male', '789 Pine Rd, City', 'Fever', 'REGULAR', 'Maria Dela Cruz', '0934-567-8902', FALSE, 'B+', '2025-01-05', '2025-11-28'),
('A142', 'Ana', 'Reyes', 28, '0945-678-9012', 'Female', '321 Elm St, City', 'Cough', 'REGULAR', 'Pedro Reyes', '0945-678-9013', FALSE, 'AB+', '2025-01-01', '2025-11-27'),
('A138', 'Sofia', 'Villanueva', 35, '0956-789-0123', 'Female', '654 Maple Dr, City', 'Fracture', 'REGULAR', 'Carlos Villanueva', '0956-789-0124', FALSE, 'O-', '2024-12-20', '2025-11-25');

-- Insert sample tickets
INSERT INTO tickets (visit_id, ticket_number, patient_id, status, priority, service_type, assigned_doctor_id, created_time, wait_time_minutes) VALUES
('Q001', 'A1', 'A145', 'IN_SERVICE', 'REGULAR', 'Consultation', 'DOC001', '2025-11-28 10:00:00', 15),
('Q002', 'A2', 'A144', 'WAITING', 'EMERGENCY', 'Consultation', 'DOC002', '2025-11-28 10:05:00', 8),
('Q003', 'A3', 'A143', 'COMPLETED', 'REGULAR', 'Consultation', 'DOC001', '2025-11-28 09:30:00', 22);

-- Insert sample appointments
INSERT INTO appointments (appointment_id, patient_id, doctor_id, appointment_date, appointment_time, purpose, status, notes) VALUES
('APT001', 'A145', 'DOC001', '2025-12-01', '09:00:00', 'Follow-up consultation', 'SCHEDULED', 'Regular checkup'),
('APT002', 'A144', 'DOC002', '2025-12-05', '11:30:00', 'Cardiac evaluation', 'CONFIRMED', 'Patient confirmed via phone'),
('APT003', 'A142', 'DOC001', '2025-12-14', '14:00:00', 'General checkup', 'SCHEDULED', NULL);

-- Insert sample prescriptions
INSERT INTO prescriptions (prescription_id, patient_id, doctor_id, medication, dosage, frequency, consultation_notes, diagnosis, treatment_plan, consultation_date) VALUES
('PRES001', 'A143', 'DOC001', 'Paracetamol', '500 mg', '3x daily for 5 days', 'Patient advised to rest and stay hydrated. Follow up if symptoms persist.', 'Fever', 'Rest, hydration, medication', '2025-11-28 10:45:00'),
('PRES002', 'A142', 'DOC001', 'Cough Syrup', '10 ml', '3x daily', 'Patient has persistent cough. Monitor for improvement.', 'Cough', 'Medication and rest', '2025-11-27 14:30:00');

-- Insert sample discharges
INSERT INTO discharges (discharge_id, patient_id, department, status, prescription_id, discharge_date, billing_amount, notes) VALUES
('DIS001', 'A143', 'General', 'CLEARED', 'PRES001', '2025-11-28 11:00:00', 1500.00, 'Patient cleared for discharge'),
('DIS002', 'A144', 'Cardiology', 'READY', NULL, NULL, NULL, 'Pending review'),
('DIS003', 'A138', 'Orthopedics', 'CLEARED', NULL, '2025-11-25 16:00:00', 5000.00, 'Fracture treated, follow-up scheduled');

-- Insert sample activity logs
INSERT INTO activity_logs (user_id, action, details, activity_type, ip_address, timestamp) VALUES
('U003', 'Login', 'User logged into system', 'LOGIN', '192.168.1.100', '2025-11-28 08:00:00'),
('U003', 'Create Patient', 'Registered new patient: A145', 'PATIENT', '192.168.1.100', '2025-11-28 09:00:00'),
('U002', 'Update Queue', 'Called patient Q001', 'QUEUE', '192.168.1.101', '2025-11-28 10:00:00'),
('U002', 'Create Prescription', 'Prescribed medication for patient A143', 'DISCHARGE', '192.168.1.101', '2025-11-28 10:45:00');

-- =====================================================
-- VIEWS (Optional - for easier queries)
-- =====================================================

-- View: Active Patients in Queue
CREATE OR REPLACE VIEW v_active_queue AS
SELECT
    t.visit_id,
    t.ticket_number,
    p.patient_id,
    CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
    p.age,
    p.phone_number,
    p.chief_complaint,
    t.status,
    t.priority,
    t.wait_time_minutes,
    d.name AS doctor_name,
    t.created_time
FROM tickets t
INNER JOIN patients p ON t.patient_id = p.patient_id
LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id
WHERE t.status IN ('WAITING', 'CALLED', 'IN_SERVICE')
ORDER BY
    CASE t.priority
        WHEN 'EMERGENCY' THEN 1
        WHEN 'SENIOR_CITIZEN' THEN 2
        WHEN 'REGULAR' THEN 3
    END,
    t.created_time;

-- View: Today's Appointments
CREATE OR REPLACE VIEW v_today_appointments AS
SELECT
    a.appointment_id,
    a.appointment_time,
    CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
    p.phone_number,
    d.name AS doctor_name,
    a.purpose,
    a.status
FROM appointments a
INNER JOIN patients p ON a.patient_id = p.patient_id
INNER JOIN doctors d ON a.doctor_id = d.doctor_id
WHERE a.appointment_date = CURDATE()
ORDER BY a.appointment_time;

-- View: Patient Statistics
CREATE OR REPLACE VIEW v_patient_stats AS
SELECT
    COUNT(*) AS total_patients,
    COUNT(CASE WHEN priority = 'EMERGENCY' THEN 1 END) AS emergency_count,
    COUNT(CASE WHEN priority = 'SENIOR_CITIZEN' THEN 1 END) AS senior_citizen_count,
    COUNT(CASE WHEN is_senior_citizen = TRUE THEN 1 END) AS senior_citizens,
    COUNT(CASE WHEN last_visit_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) AS active_last_month
FROM patients;

-- =====================================================
-- STORED PROCEDURES (Optional - for common operations)
-- =====================================================

-- Procedure: Get Next Queue Number
DELIMITER //
CREATE PROCEDURE sp_get_next_queue_number()
BEGIN
    DECLARE next_num INT;
    SELECT COALESCE(MAX(CAST(SUBSTRING(ticket_number, 2) AS UNSIGNED)), 0) + 1
    INTO next_num
    FROM tickets
    WHERE ticket_number LIKE CONCAT(SUBSTRING(CAST(DATE_FORMAT(NOW(), '%m') AS CHAR), -1), '%')
    AND DATE(created_time) = CURDATE();

    SELECT CONCAT(CHAR(64 + FLOOR((next_num - 1) / 10) + 1), ((next_num - 1) % 10) + 1) AS next_ticket_number;
END //
DELIMITER ;

-- Procedure: Update Ticket Status
DELIMITER //
CREATE PROCEDURE sp_update_ticket_status(
    IN p_visit_id VARCHAR(20),
    IN p_status VARCHAR(20)
)
BEGIN
    UPDATE tickets
    SET status = p_status,
        updated_at = NOW()
    WHERE visit_id = p_visit_id;

    IF p_status = 'CALLED' THEN
        UPDATE tickets
        SET called_time = NOW()
        WHERE visit_id = p_visit_id;
    ELSEIF p_status = 'COMPLETED' THEN
        UPDATE tickets
        SET completed_time = NOW(),
            wait_time_minutes = TIMESTAMPDIFF(MINUTE, created_time, NOW())
        WHERE visit_id = p_visit_id;
    END IF;
END //
DELIMITER ;

-- =====================================================
-- TRIGGERS (Optional - for automatic updates)
-- =====================================================

-- Trigger: Update patient last_visit_date when ticket is completed
DELIMITER //
CREATE TRIGGER trg_update_patient_last_visit
AFTER UPDATE ON tickets
FOR EACH ROW
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        UPDATE patients
        SET last_visit_date = CURDATE()
        WHERE patient_id = NEW.patient_id;
    END IF;
END //
DELIMITER ;

-- Trigger: Log user activity on login
DELIMITER //
CREATE TRIGGER trg_log_user_login
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF NEW.last_active IS NOT NULL AND (OLD.last_active IS NULL OR NEW.last_active != OLD.last_active) THEN
        INSERT INTO activity_logs (user_id, action, details, activity_type, timestamp)
        VALUES (NEW.user_id, 'Login', CONCAT('User ', NEW.full_name, ' logged in'), 'LOGIN', NOW());
    END IF;
END //
DELIMITER ;

-- =====================================================
-- END OF SCHEMA
-- =====================================================

