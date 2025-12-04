package com.stangelo.saintangelo.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.stangelo.saintangelo.models.Patient;

/**
 * Data Access Object for Patient entity
 * Handles all database operations related to patients
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class PatientDAO extends BaseDAO {

    /**
     * Finds a patient by ID
     *
     * @param patientId Patient ID
     * @return Patient object if found, null otherwise
     */
    public Patient findById(String patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding patient by ID: " + patientId, e);
        }
        return null;
    }

    /**
     * Finds patients by name (first name or last name)
     *
     * @param name Name to search for
     * @return List of matching patients
     */
    public List<Patient> findByName(String name) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + name + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding patients by name: " + name, e);
        }
        return patients;
    }

    /**
     * Finds a patient by phone number
     *
     * @param phoneNumber Phone number
     * @return Patient object if found, null otherwise
     */
    public Patient findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM patients WHERE phone_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding patient by phone: " + phoneNumber, e);
        }
        return null;
    }

    /**
     * Gets all patients
     *
     * @return List of all patients
     */
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY last_name, first_name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            logError("Error finding all patients", e);
        }
        return patients;
    }

    /**
     * Creates a new patient
     *
     * @param patient Patient object to create
     * @return true if successful, false otherwise
     */
    public boolean create(Patient patient) {
        String sql = "INSERT INTO patients (patient_id, first_name, last_name, age, phone_number, gender, " +
                "home_address, chief_complaint, priority, emergency_contact_person, emergency_contact_number, " +
                "is_senior_citizen, blood_type, registration_date, last_visit_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getId());
            stmt.setString(2, extractFirstName(patient.getName()));
            stmt.setString(3, extractLastName(patient.getName()));
            stmt.setInt(4, patient.getAge());
            stmt.setString(5, patient.getContactNumber());
            stmt.setString(6, patient.getGender());
            stmt.setString(7, patient.getHomeAddress());
            // Note: Patient model needs getChiefComplaint method
            // Using notes field as temporary storage
            stmt.setString(8, patient.getNotes());
            stmt.setString(9, mapPriorityToString(patient));
            stmt.setString(10, patient.getEmergencycontactPerson());
            stmt.setString(11, patient.getEmergencycontactNumber());
            stmt.setBoolean(12, patient.isSeniorCitizen());
            stmt.setString(13, patient.getBloodType());
            stmt.setDate(14, Date.valueOf(LocalDate.now()));
            stmt.setDate(15, patient.getLastVisitDate() != null ?
                    Date.valueOf(LocalDate.parse(patient.getLastVisitDate())) : null);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating patient: " + patient.getId(), e);
            return false;
        }
    }

    /**
     * Updates an existing patient
     *
     * @param patient Patient object with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, age = ?, phone_number = ?, gender = ?, " +
                "home_address = ?, chief_complaint = ?, priority = ?, emergency_contact_person = ?, " +
                "emergency_contact_number = ?, is_senior_citizen = ?, blood_type = ?, last_visit_date = ? " +
                "WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, extractFirstName(patient.getName()));
            stmt.setString(2, extractLastName(patient.getName()));
            stmt.setInt(3, patient.getAge());
            stmt.setString(4, patient.getContactNumber());
            stmt.setString(5, patient.getGender());
            stmt.setString(6, patient.getHomeAddress());
            // Note: Patient model needs getChiefComplaint method
            stmt.setString(7, patient.getNotes());
            stmt.setString(8, mapPriorityToString(patient));
            stmt.setString(9, patient.getEmergencycontactPerson());
            stmt.setString(10, patient.getEmergencycontactNumber());
            stmt.setBoolean(11, patient.isSeniorCitizen());
            stmt.setString(12, patient.getBloodType());
            stmt.setDate(13, patient.getLastVisitDate() != null ?
                    Date.valueOf(LocalDate.parse(patient.getLastVisitDate())) : null);
            stmt.setString(14, patient.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating patient: " + patient.getId(), e);
            return false;
        }
    }

    /**
     * Updates patient's last visit date
     *
     * @param patientId Patient ID
     * @param visitDate Visit date
     * @return true if successful, false otherwise
     */
    public boolean updateLastVisitDate(String patientId, LocalDate visitDate) {
        String sql = "UPDATE patients SET last_visit_date = ? WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(visitDate));
            stmt.setString(2, patientId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating last visit date for patient: " + patientId, e);
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a Patient object
     */
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        String id = rs.getString("patient_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String name = firstName + " " + lastName;
        int age = rs.getInt("age");
        String phoneNumber = rs.getString("phone_number");
        String gender = rs.getString("gender");
        String homeAddress = rs.getString("home_address");
        String chiefComplaint = rs.getString("chief_complaint");
        String emergencyContactPerson = rs.getString("emergency_contact_person");
        String emergencyContactNumber = rs.getString("emergency_contact_number");
        boolean isSeniorCitizen = rs.getBoolean("is_senior_citizen");
        String bloodType = rs.getString("blood_type");

        Date lastVisitDate = rs.getDate("last_visit_date");
        String lastVisitDateStr = lastVisitDate != null ? lastVisitDate.toString() : null;

        Patient patient = new Patient(id, name, age, phoneNumber, homeAddress, gender,
                emergencyContactPerson, emergencyContactNumber, isSeniorCitizen,
                null, null, null, null, null, null, null, null, null, null, null, lastVisitDateStr, bloodType);

        // Note: Patient model needs setChiefComplaint method
        // For now, we'll store it in notes or another field
        if (chiefComplaint != null) {
            patient.setNotes(chiefComplaint);
        }

        return patient;
    }

    /**
     * Helper method to extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * Helper method to extract last name from full name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "";
    }

    /**
     * Helper method to map priority from Patient to database string
     */
    private String mapPriorityToString(Patient patient) {
        if (patient.isSeniorCitizen()) {
            return "SENIOR_CITIZEN";
        }
        return "REGULAR";
    }

    /**
     * Gets patient's chief complaint from database
     */
    public String getChiefComplaint(String patientId) {
        String sql = "SELECT chief_complaint FROM patients WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("chief_complaint");
                }
            }
        } catch (SQLException e) {
            logError("Error getting chief complaint for patient: " + patientId, e);
        }
        return null;
    }

    /**
     * Gets total number of patients in the system.
     *
     * @return total patient count
     */
    public int countAllPatients() {
        String sql = "SELECT COUNT(*) FROM patients";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("Error counting all patients", e);
        }

        return 0;
    }
}

