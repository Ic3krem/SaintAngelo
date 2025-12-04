package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.Doctor;
import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.Prescription;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Prescription entity
 */
public class PrescriptionDAO extends BaseDAO {

    public Prescription findById(String prescriptionId) {
        String sql = "SELECT * FROM prescriptions WHERE prescription_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescriptionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPrescription(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding prescription by ID: " + prescriptionId, e);
        }
        return null;
    }

    public List<Prescription> findByPatient(String patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? ORDER BY consultation_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding prescriptions by patient: " + patientId, e);
        }
        return prescriptions;
    }

    /**
     * Finds prescriptions by patient and doctor (for a specific visit)
     *
     * @param patientId Patient ID
     * @param doctorId Doctor ID
     * @return List of prescriptions matching patient and doctor, ordered by date DESC
     */
    public List<Prescription> findByPatientAndDoctor(String patientId, String doctorId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? AND doctor_id = ? ORDER BY consultation_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setString(2, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding prescriptions by patient and doctor: " + patientId + ", " + doctorId, e);
        }
        return prescriptions;
    }

    public boolean create(Prescription prescription) {
        String sql = "INSERT INTO prescriptions (prescription_id, patient_id, doctor_id, medication, dosage, " +
                "frequency, consultation_notes, diagnosis, treatment_plan, consultation_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescription.getPrescriptionId());
            stmt.setString(2, prescription.getPatient().getId());
            stmt.setString(3, prescription.getDoctor().getId());
            stmt.setString(4, prescription.getMedication());
            stmt.setString(5, prescription.getDosage());
            stmt.setString(6, prescription.getFrequency());
            stmt.setString(7, prescription.getConsultationNotes());
            stmt.setString(8, prescription.getDiagnosis());
            stmt.setString(9, prescription.getTreatmentPlan());
            stmt.setTimestamp(10, Timestamp.valueOf(prescription.getConsultationDate()));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating prescription: " + prescription.getPrescriptionId(), e);
            return false;
        }
    }

    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        String prescriptionId = rs.getString("prescription_id");
        String patientId = rs.getString("patient_id");
        String doctorId = rs.getString("doctor_id");
        String medication = rs.getString("medication");
        String dosage = rs.getString("dosage");
        String frequency = rs.getString("frequency");
        String consultationNotes = rs.getString("consultation_notes");
        String diagnosis = rs.getString("diagnosis");
        String treatmentPlan = rs.getString("treatment_plan");
        LocalDateTime consultationDate = rs.getTimestamp("consultation_date").toLocalDateTime();

        Patient patient = new PatientDAO().findById(patientId);
        Doctor doctor = new DoctorDAO().findById(doctorId);

        if (patient == null || doctor == null) {
            throw new SQLException("Patient or Doctor not found for prescription: " + prescriptionId);
        }

        return new Prescription(prescriptionId, patient, doctor, medication, dosage, frequency,
                consultationNotes, consultationDate, diagnosis, treatmentPlan);
    }
}

