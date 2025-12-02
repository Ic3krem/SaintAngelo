package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.Appointment;
import com.stangelo.saintangelo.models.AppointmentStatus;
import com.stangelo.saintangelo.models.Doctor;
import com.stangelo.saintangelo.models.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Appointment entity
 * Handles all database operations related to appointments
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class AppointmentDAO extends BaseDAO {

    /**
     * Finds an appointment by ID
     */
    public Appointment findById(String appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppointment(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding appointment by ID: " + appointmentId, e);
        }
        return null;
    }

    /**
     * Gets appointments for a specific date
     */
    public List<Appointment> findByDate(LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE appointment_date = ? ORDER BY appointment_time";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding appointments by date: " + date, e);
        }
        return appointments;
    }

    /**
     * Gets appointments for a specific doctor
     */
    public List<Appointment> findByDoctor(String doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_date, appointment_time";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding appointments by doctor: " + doctorId, e);
        }
        return appointments;
    }

    /**
     * Gets appointments for a specific patient
     */
    public List<Appointment> findByPatient(String patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC, appointment_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding appointments by patient: " + patientId, e);
        }
        return appointments;
    }

    /**
     * Creates a new appointment
     */
    public boolean create(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_id, patient_id, doctor_id, appointment_date, " +
                "appointment_time, purpose, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointment.getAppointmentId());
            stmt.setString(2, appointment.getPatient().getId());
            stmt.setString(3, appointment.getDoctor().getId());
            stmt.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            stmt.setString(6, appointment.getPurpose());
            stmt.setString(7, appointment.getStatus().name());
            stmt.setString(8, appointment.getNotes());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating appointment: " + appointment.getAppointmentId(), e);
            return false;
        }
    }

    /**
     * Updates appointment status
     */
    public boolean updateStatus(String appointmentId, AppointmentStatus status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating appointment status: " + appointmentId, e);
            return false;
        }
    }

    /**
     * Maps ResultSet to Appointment object
     */
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        String appointmentId = rs.getString("appointment_id");
        String patientId = rs.getString("patient_id");
        String doctorId = rs.getString("doctor_id");
        LocalDate appointmentDate = rs.getDate("appointment_date").toLocalDate();
        LocalTime appointmentTime = rs.getTime("appointment_time").toLocalTime();
        String purpose = rs.getString("purpose");
        AppointmentStatus status = AppointmentStatus.valueOf(rs.getString("status"));
        String notes = rs.getString("notes");

        Patient patient = new PatientDAO().findById(patientId);
        Doctor doctor = new DoctorDAO().findById(doctorId);

        if (patient == null || doctor == null) {
            throw new SQLException("Patient or Doctor not found for appointment: " + appointmentId);
        }

        Appointment appointment = new Appointment(appointmentId, patient, doctor, appointmentDate,
                appointmentTime, purpose, status, notes);

        return appointment;
    }
}

