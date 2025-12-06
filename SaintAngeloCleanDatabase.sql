-- =====================================================
-- SaintAngelo Hospital Queue Management System
-- Database Cleaning Script
-- MySQL Database Cleanup Schema
-- =====================================================
-- Purpose: Remove all data from all tables while preserving structure
-- WARNING: This will delete ALL data from the database!
-- Use with caution in production environments
-- =====================================================

USE saintangelo_hospital;

-- =====================================================
-- DISABLE FOREIGN KEY CHECKS
-- This allows us to truncate tables in any order
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- TRUNCATE ALL TABLES
-- Truncate removes all data but preserves table structure
-- =====================================================

-- 1. Child tables (tables with foreign keys)
TRUNCATE TABLE discharges;
TRUNCATE TABLE prescriptions;
TRUNCATE TABLE appointments;
TRUNCATE TABLE tickets;
TRUNCATE TABLE reports;
TRUNCATE TABLE activity_logs;

-- 2. Parent tables (tables referenced by foreign keys)
TRUNCATE TABLE doctors;
TRUNCATE TABLE patients;
TRUNCATE TABLE users;

-- =====================================================
-- RE-ENABLE FOREIGN KEY CHECKS
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICATION QUERIES (Optional)
-- Uncomment to verify tables are empty
-- =====================================================
-- SELECT 'discharges' AS table_name, COUNT(*) AS row_count FROM discharges
-- UNION ALL
-- SELECT 'prescriptions', COUNT(*) FROM prescriptions
-- UNION ALL
-- SELECT 'appointments', COUNT(*) FROM appointments
-- UNION ALL
-- SELECT 'tickets', COUNT(*) FROM tickets
-- UNION ALL
-- SELECT 'reports', COUNT(*) FROM reports
-- UNION ALL
-- SELECT 'activity_logs', COUNT(*) FROM activity_logs
-- UNION ALL
-- SELECT 'doctors', COUNT(*) FROM doctors
-- UNION ALL
-- SELECT 'patients', COUNT(*) FROM patients
-- UNION ALL
-- SELECT 'users', COUNT(*) FROM users;

-- =====================================================
-- ALTERNATIVE: DELETE WITH CASCADE (if truncate doesn't work)
-- Uncomment the section below if TRUNCATE fails
-- =====================================================
/*
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM discharges;
DELETE FROM prescriptions;
DELETE FROM appointments;
DELETE FROM tickets;
DELETE FROM reports;
DELETE FROM activity_logs;
DELETE FROM doctors;
DELETE FROM patients;
DELETE FROM users;

SET FOREIGN_KEY_CHECKS = 1;
*/

-- =====================================================
-- RESET AUTO_INCREMENT COUNTERS (if needed)
-- Uncomment if you want to reset auto-increment values
-- =====================================================
-- ALTER TABLE activity_logs AUTO_INCREMENT = 1;

-- =====================================================
-- END OF CLEANING SCRIPT
-- =====================================================
