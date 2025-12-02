package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.models.TicketStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Ticket entity
 * Handles all database operations related to queue tickets
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class TicketDAO extends BaseDAO {

    /**
     * Finds a ticket by visit ID
     *
     * @param visitId Visit ID
     * @return Ticket object if found, null otherwise
     */
    public Ticket findByVisitId(String visitId) {
        String sql = "SELECT t.*, p.* FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "WHERE t.visit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, visitId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding ticket by visit ID: " + visitId, e);
        }
        return null;
    }

    /**
     * Gets all active tickets (WAITING, CALLED, IN_SERVICE)
     *
     * @return List of active tickets
     */
    public List<Ticket> findActiveTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.* FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "WHERE t.status IN ('WAITING', 'CALLED', 'IN_SERVICE') " +
                "ORDER BY " +
                "CASE t.priority " +
                "  WHEN 'EMERGENCY' THEN 1 " +
                "  WHEN 'SENIOR_CITIZEN' THEN 2 " +
                "  WHEN 'REGULAR' THEN 3 " +
                "END, t.created_time";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            logError("Error finding active tickets", e);
        }
        return tickets;
    }

    /**
     * Gets tickets by status
     *
     * @param status Ticket status
     * @return List of tickets with the specified status
     */
    public List<Ticket> findByStatus(TicketStatus status) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.* FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "WHERE t.status = ? ORDER BY t.created_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding tickets by status: " + status, e);
        }
        return tickets;
    }

    /**
     * Gets tickets for today
     *
     * @return List of today's tickets
     */
    public List<Ticket> findTodayTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.* FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "WHERE DATE(t.created_time) = CURDATE() " +
                "ORDER BY t.created_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            logError("Error finding today's tickets", e);
        }
        return tickets;
    }

    /**
     * Creates a new ticket
     *
     * @param ticket Ticket object to create
     * @return true if successful, false otherwise
     */
    public boolean create(Ticket ticket) {
        String sql = "INSERT INTO tickets (visit_id, ticket_number, patient_id, status, priority, " +
                "service_type, assigned_doctor_id, created_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticket.getVisitId());
            stmt.setString(2, ticket.getTicketNumber());
            stmt.setString(3, ticket.getPatient().getId());
            stmt.setString(4, ticket.getStatus().name());
            stmt.setString(5, "REGULAR"); // Default priority
            stmt.setString(6, ticket.getServiceType());
            stmt.setString(7, null); // No assigned doctor initially
            stmt.setTimestamp(8, Timestamp.valueOf(ticket.getCreatedTime()));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating ticket: " + ticket.getVisitId(), e);
            return false;
        }
    }

    /**
     * Updates ticket status
     *
     * @param visitId Visit ID
     * @param status New status
     * @return true if successful, false otherwise
     */
    public boolean updateStatus(String visitId, TicketStatus status) {
        String sql = "UPDATE tickets SET status = ?";

        // Update timestamps based on status
        if (status == TicketStatus.CALLED) {
            sql += ", called_time = NOW()";
        } else if (status == TicketStatus.COMPLETED) {
            sql += ", completed_time = NOW(), wait_time_minutes = TIMESTAMPDIFF(MINUTE, created_time, NOW())";
        }

        sql += " WHERE visit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, visitId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating ticket status: " + visitId, e);
            return false;
        }
    }

    /**
     * Assigns a doctor to a ticket
     *
     * @param visitId Visit ID
     * @param doctorId Doctor ID
     * @return true if successful, false otherwise
     */
    public boolean assignDoctor(String visitId, String doctorId) {
        String sql = "UPDATE tickets SET assigned_doctor_id = ? WHERE visit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorId);
            stmt.setString(2, visitId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error assigning doctor to ticket: " + visitId, e);
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a Ticket object
     */
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        String visitId = rs.getString("visit_id");
        String ticketNumber = rs.getString("ticket_number");
        TicketStatus status = TicketStatus.valueOf(rs.getString("status"));
        String serviceType = rs.getString("service_type");

        Timestamp createdTimeTs = rs.getTimestamp("created_time");
        LocalDateTime createdTime = createdTimeTs != null ? createdTimeTs.toLocalDateTime() : LocalDateTime.now();

        // Create patient object (simplified - you may need to load full patient details)
        Patient patient = new PatientDAO().findById(rs.getString("patient_id"));
        if (patient == null) {
            throw new SQLException("Patient not found for ticket: " + visitId);
        }

        return new Ticket(visitId, ticketNumber, patient, status, createdTime, serviceType);
    }
}

