package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Doctor entity
 * Handles all database operations related to doctors
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class DoctorDAO extends BaseDAO {

    /**
     * Finds a doctor by ID
     *
     * @param doctorId Doctor ID
     * @return Doctor object if found, null otherwise
     */
    public Doctor findById(String doctorId) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding doctor by ID: " + doctorId, e);
        }
        return null;
    }

    /**
     * Finds a doctor by user ID
     *
     * @param userId User ID
     * @return Doctor object if found, null otherwise
     */
    public Doctor findByUserId(String userId) {
        String sql = "SELECT * FROM doctors WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding doctor by user ID: " + userId, e);
        }
        return null;
    }

    /**
     * Gets all active doctors
     *
     * @return List of active doctors
     */
    public List<Doctor> findAllActive() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE status = 'Active' ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            logError("Error finding all active doctors", e);
        }
        return doctors;
    }

    /**
     * Gets all doctors
     *
     * @return List of all doctors
     */
    public List<Doctor> findAll() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            logError("Error finding all doctors", e);
        }
        return doctors;
    }

    /**
     * Finds doctors by department
     *
     * @param department Department name
     * @return List of doctors in the department
     */
    public List<Doctor> findByDepartment(String department) {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE department = ? AND status = 'Active' ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding doctors by department: " + department, e);
        }
        return doctors;
    }

    /**
     * Creates a new doctor
     *
     * @param doctor Doctor object to create
     * @param userId User ID (optional)
     * @param email Email address
     * @param department Department name
     * @return true if successful, false otherwise
     */
    public boolean create(Doctor doctor, String userId, String email, String department) {
        String sql = "INSERT INTO doctors (doctor_id, user_id, name, email, specialization, department, building_number, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'Active')";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getId());
            stmt.setString(2, userId);
            stmt.setString(3, doctor.getName());
            stmt.setString(4, email);
            stmt.setString(5, doctor.getSpecialization());
            stmt.setString(6, department);
            stmt.setInt(7, doctor.getBuildingNumber());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating doctor: " + doctor.getId(), e);
            return false;
        }
    }

    /**
     * Updates an existing doctor
     *
     * @param doctor Doctor object with updated information
     * @param email Email address
     * @param department Department name
     * @return true if successful, false otherwise
     */
    public boolean update(Doctor doctor, String email, String department) {
        String sql = "UPDATE doctors SET name = ?, email = ?, specialization = ?, department = ?, " +
                "building_number = ? WHERE doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getName());
            stmt.setString(2, email);
            stmt.setString(3, doctor.getSpecialization());
            stmt.setString(4, department);
            stmt.setInt(5, doctor.getBuildingNumber());
            stmt.setString(6, doctor.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating doctor: " + doctor.getId(), e);
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a Doctor object
     */
    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        String id = rs.getString("doctor_id");
        String name = rs.getString("name");
        String specialization = rs.getString("specialization");
        int buildingNumber = rs.getInt("building_number");

        return new Doctor(id, name, specialization, buildingNumber);
    }
}

