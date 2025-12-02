package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.Discharge;
import com.stangelo.saintangelo.models.DischargeStatus;
import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.Prescription;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Discharge entity
 */
public class DischargeDAO extends BaseDAO {

    public Discharge findById(String dischargeId) {
        String sql = "SELECT * FROM discharges WHERE discharge_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dischargeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDischarge(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding discharge by ID: " + dischargeId, e);
        }
        return null;
    }

    public List<Discharge> findByStatus(DischargeStatus status) {
        List<Discharge> discharges = new ArrayList<>();
        String sql = "SELECT * FROM discharges WHERE status = ? ORDER BY discharge_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    discharges.add(mapResultSetToDischarge(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding discharges by status: " + status, e);
        }
        return discharges;
    }

    public boolean create(Discharge discharge) {
        String sql = "INSERT INTO discharges (discharge_id, patient_id, department, status, prescription_id, " +
                "discharge_date, billing_amount, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, discharge.getDischargeId());
            stmt.setString(2, discharge.getPatient().getId());
            stmt.setString(3, discharge.getDepartment());
            stmt.setString(4, discharge.getStatus().name());
            stmt.setString(5, discharge.getPrescription() != null ? discharge.getPrescription().getPrescriptionId() : null);
            stmt.setTimestamp(6, discharge.getDischargeDate() != null ? Timestamp.valueOf(discharge.getDischargeDate()) : null);
            stmt.setBigDecimal(7, discharge.getBillingAmount() != null ? new BigDecimal(discharge.getBillingAmount()) : null);
            stmt.setString(8, discharge.getNotes());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating discharge: " + discharge.getDischargeId(), e);
            return false;
        }
    }

    public boolean updateStatus(String dischargeId, DischargeStatus status) {
        String sql = "UPDATE discharges SET status = ? WHERE discharge_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, dischargeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating discharge status: " + dischargeId, e);
            return false;
        }
    }

    private Discharge mapResultSetToDischarge(ResultSet rs) throws SQLException {
        String dischargeId = rs.getString("discharge_id");
        String patientId = rs.getString("patient_id");
        String department = rs.getString("department");
        DischargeStatus status = DischargeStatus.valueOf(rs.getString("status"));
        String prescriptionId = rs.getString("prescription_id");
        Timestamp dischargeDateTs = rs.getTimestamp("discharge_date");
        LocalDateTime dischargeDate = dischargeDateTs != null ? dischargeDateTs.toLocalDateTime() : null;
        BigDecimal billingAmount = rs.getBigDecimal("billing_amount");
        String notes = rs.getString("notes");

        Patient patient = new PatientDAO().findById(patientId);
        Prescription prescription = prescriptionId != null ? new PrescriptionDAO().findById(prescriptionId) : null;

        if (patient == null) {
            throw new SQLException("Patient not found for discharge: " + dischargeId);
        }

        return new Discharge(dischargeId, patient, department, status, dischargeDate, prescription,
                billingAmount != null ? billingAmount.toString() : null, notes);
    }
}

