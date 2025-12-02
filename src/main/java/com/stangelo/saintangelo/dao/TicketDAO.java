package com.stangelo.saintangelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.PriorityLevel;
import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.models.TicketStatus;

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
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
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
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
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
                try {
                tickets.add(mapResultSetToTicket(rs));
                } catch (SQLException e) {
                    logError("Error mapping ticket from result set", e);
                }
            }
        } catch (SQLException e) {
            logError("Error finding active tickets", e);
        }
        return tickets;
    }

    /**
     * Gets waiting tickets only (for queue display)
     * Limited to top N for performance
     *
     * @param limit Maximum number of tickets to return
     * @return List of waiting tickets
     */
    public List<Ticket> findWaitingTickets(int limit) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = 'WAITING' AND DATE(t.created_time) = CURDATE() " +
                "ORDER BY " +
                "CASE t.priority " +
                "  WHEN 'EMERGENCY' THEN 1 " +
                "  WHEN 'SENIOR_CITIZEN' THEN 2 " +
                "  WHEN 'REGULAR' THEN 3 " +
                "END, t.created_time " +
                "LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        tickets.add(mapResultSetToTicket(rs));
                    } catch (SQLException e) {
                        // Log but continue processing other tickets
                        logError("Error mapping ticket from result set", e);
                    }
                }
            }
        } catch (SQLException e) {
            logError("Error finding waiting tickets", e);
        }
        return tickets;
    }

    /**
     * Gets the currently serving ticket (IN_SERVICE status with assigned doctor)
     *
     * @return The ticket currently being served, or null if none
     */
    public Ticket findCurrentlyServing() {
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = 'IN_SERVICE' AND DATE(t.created_time) = CURDATE() " +
                "ORDER BY t.called_time DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            logError("Error finding currently serving ticket", e);
        }
        return null;
    }

    /**
     * Finds the currently serving ticket for a specific doctor
     *
     * @param doctorId Doctor ID
     * @return The ticket currently being served by this doctor, or null
     */
    public Ticket findCurrentlyServingByDoctor(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) {
            return null;
        }
        
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = 'IN_SERVICE' AND t.assigned_doctor_id = ? " +
                "ORDER BY t.called_time DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding currently serving ticket for doctor: " + doctorId, e);
        }
        return null;
    }

    /**
     * Gets the next ticket to be called (first WAITING ticket)
     *
     * @return The next ticket in queue, or null if queue is empty
     */
    public Ticket findNextInQueue() {
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = 'WAITING' AND DATE(t.created_time) = CURDATE() " +
                "ORDER BY " +
                "CASE t.priority " +
                "  WHEN 'EMERGENCY' THEN 1 " +
                "  WHEN 'SENIOR_CITIZEN' THEN 2 " +
                "  WHEN 'REGULAR' THEN 3 " +
                "END, t.created_time " +
                "LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            logError("Error finding next ticket in queue", e);
        }
        return null;
    }

    /**
     * Gets tickets by status
     *
     * @param status Ticket status
     * @return List of tickets with the specified status
     */
    public List<Ticket> findByStatus(TicketStatus status) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = ? ORDER BY t.created_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                    tickets.add(mapResultSetToTicket(rs));
                    } catch (SQLException e) {
                        logError("Error mapping ticket from result set", e);
                    }
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
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE DATE(t.created_time) = CURDATE() " +
                "ORDER BY t.created_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                tickets.add(mapResultSetToTicket(rs));
                } catch (SQLException e) {
                    logError("Error mapping ticket from result set", e);
                }
            }
        } catch (SQLException e) {
            logError("Error finding today's tickets", e);
        }
        return tickets;
    }

    /**
     * Gets the count of waiting tickets for today
     *
     * @return Number of waiting tickets
     */
    public int countWaitingTickets() {
        String sql = "SELECT COUNT(*) FROM tickets WHERE status = 'WAITING' AND DATE(created_time) = CURDATE()";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("Error counting waiting tickets", e);
        }
        return 0;
    }

    /**
     * Gets the count of all tickets created today
     *
     * @return Number of tickets created today
     */
    public int countTodayTickets() {
        String sql = "SELECT COUNT(*) FROM tickets WHERE DATE(created_time) = CURDATE()";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("Error counting today's tickets", e);
        }
        return 0;
    }

    /**
     * Gets the average wait time in minutes for tickets that have been called today
     * Wait time is calculated from created_time to called_time (when patient was called)
     * Includes tickets with status CALLED, IN_SERVICE, or COMPLETED
     *
     * @return Average wait time in minutes, or 0 if no tickets have been called today
     */
    public int getAverageWaitTimeToday() {
        String sql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, created_time, called_time)) AS avg_wait_time " +
                     "FROM tickets " +
                     "WHERE DATE(created_time) = CURDATE() " +
                     "AND called_time IS NOT NULL " +
                     "AND status IN ('CALLED', 'IN_SERVICE', 'COMPLETED')";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                double avgWaitTime = rs.getDouble("avg_wait_time");
                if (!rs.wasNull()) {
                    return (int) Math.round(avgWaitTime);
                }
            }
        } catch (SQLException e) {
            logError("Error calculating average wait time", e);
        }
        return 0;
    }

    /**
     * Gets the next ticket number for today
     * Follows the format A1-A10, B1-B10, etc.
     *
     * @return The next ticket number string
     */
    public String getNextTicketNumber() {
        // Get all ticket numbers for today and find the maximum
        String sql = "SELECT ticket_number FROM tickets WHERE DATE(created_time) = CURDATE()";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            String maxTicket = null;
            int maxValue = -1;

            while (rs.next()) {
                String ticketNumber = rs.getString("ticket_number");
                if (ticketNumber != null && ticketNumber.length() >= 2) {
                    try {
                        char letter = ticketNumber.charAt(0);
                        int number = Integer.parseInt(ticketNumber.substring(1));
                        int value = (letter - 'A') * 10 + number;
                        
                        if (value > maxValue) {
                            maxValue = value;
                            maxTicket = ticketNumber;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid ticket numbers
                        continue;
                    }
                }
            }

            if (maxTicket != null) {
                return incrementTicketNumber(maxTicket);
            }
        } catch (SQLException e) {
            logError("Error getting next ticket number", e);
        }
        return "A1"; // Start fresh if no tickets today
    }

    /**
     * Increments a ticket number (A1 -> A2, A10 -> B1, Z10 -> A1)
     */
    private String incrementTicketNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.length() < 2) {
            return "A1";
        }
        char letter = ticketNumber.charAt(0);
        int number = Integer.parseInt(ticketNumber.substring(1));

        if (number < 10) {
            return "" + letter + (number + 1);
        } else if (letter < 'Z') {
            return "" + (char)(letter + 1) + "1";
        } else {
            return "A1"; // Wrap around
        }
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
            stmt.setString(5, ticket.getPriority() != null ? ticket.getPriority().name() : "REGULAR");
            stmt.setString(6, ticket.getServiceType());
            stmt.setString(7, ticket.getAssignedDoctorId()); // Can be null
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
     * Assigns a doctor to a ticket and updates status to IN_SERVICE
     *
     * @param visitId Visit ID
     * @param doctorId Doctor ID
     * @return true if successful, false otherwise
     */
    public boolean assignDoctor(String visitId, String doctorId) {
        String sql = "UPDATE tickets SET assigned_doctor_id = ?, status = 'IN_SERVICE', called_time = NOW() WHERE visit_id = ?";

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
     * Calls the next patient in queue (doctor action)
     * Sets status to IN_SERVICE and assigns the doctor
     *
     * @param doctorId The doctor calling the patient
     * @return The ticket that was called, or null if queue is empty
     */
    public Ticket callNextPatient(String doctorId) {
        Ticket nextTicket = findNextInQueue();
        if (nextTicket != null) {
            if (assignDoctor(nextTicket.getVisitId(), doctorId)) {
                // Refresh the ticket to get updated data
                return findByVisitId(nextTicket.getVisitId());
            }
        }
        return null;
    }

    /**
     * Gets completed tickets for today
     *
     * @return List of completed tickets for today
     */
    public List<Ticket> findCompletedToday() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = 'COMPLETED' AND DATE(t.created_time) = CURDATE() " +
                "ORDER BY t.created_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    tickets.add(mapResultSetToTicket(rs));
                } catch (SQLException e) {
                    logError("Error mapping ticket from result set", e);
                }
            }
        } catch (SQLException e) {
            logError("Error finding completed tickets for today", e);
        }
        return tickets;
    }

    /**
     * Gets all completed tickets (not just today's) with doctor department information
     * Used for discharge management
     *
     * @return List of all completed tickets with doctor info
     */
    public List<Ticket> findAllCompleted() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.*, d.name AS doctor_name, d.department AS doctor_department FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.status = 'COMPLETED' " +
                "ORDER BY t.created_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    Ticket ticket = mapResultSetToTicket(rs);
                    // Store doctor department in ticket if available
                    String doctorDept = rs.getString("doctor_department");
                    if (doctorDept != null && ticket.getAssignedDoctorName() != null) {
                        // We'll need to access this later, so we can store it in a custom way
                        // For now, we'll get it from the database when needed
                    }
                    tickets.add(ticket);
                } catch (SQLException e) {
                    logError("Error mapping ticket from result set", e);
                }
            }
        } catch (SQLException e) {
            logError("Error finding all completed tickets", e);
        }
        return tickets;
    }

    /**
     * Gets doctor department for a given doctor ID
     *
     * @param doctorId Doctor ID
     * @return Department name or null
     */
    public String getDoctorDepartment(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) {
            return null;
        }
        String sql = "SELECT department FROM doctors WHERE doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("department");
                }
            }
        } catch (SQLException e) {
            logError("Error getting doctor department: " + doctorId, e);
        }
        return null;
    }

    /**
     * Gets tickets currently in service (IN_SERVICE status)
     *
     * @return List of tickets currently in service
     */
    public List<Ticket> findInServiceTickets() {
        return findByStatus(TicketStatus.IN_SERVICE);
    }

    /**
     * Gets recently called tickets (tickets that have been called today)
     * Ordered by called_time DESC (most recent first)
     *
     * @param limit Maximum number of tickets to return
     * @return List of recently called tickets
     */
    public List<Ticket> findRecentCalls(int limit) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, p.*, d.name AS doctor_name FROM tickets t " +
                "INNER JOIN patients p ON t.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON t.assigned_doctor_id = d.doctor_id " +
                "WHERE t.called_time IS NOT NULL AND DATE(t.called_time) = CURDATE() " +
                "ORDER BY t.called_time DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        tickets.add(mapResultSetToTicket(rs));
                    } catch (SQLException e) {
                        logError("Error mapping ticket from result set", e);
                    }
                }
            }
        } catch (SQLException e) {
            logError("Error finding recent calls", e);
        }
        return tickets;
    }

    /**
     * Updates the priority of a ticket
     *
     * @param visitId Visit ID
     * @param priority New priority level
     * @return true if successful, false otherwise
     */
    public boolean updatePriority(String visitId, PriorityLevel priority) {
        String sql = "UPDATE tickets SET priority = ? WHERE visit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, priority.name());
            stmt.setString(2, visitId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating ticket priority: " + visitId, e);
            return false;
        }
    }

    /**
     * Removes/skips a ticket from the queue (sets status to SKIPPED)
     *
     * @param visitId Visit ID
     * @return true if successful, false otherwise
     */
    public boolean removeTicket(String visitId) {
        return updateStatus(visitId, TicketStatus.SKIPPED);
    }

    /**
     * Deletes a ticket from the database permanently
     *
     * @param visitId Visit ID
     * @return true if successful, false otherwise
     */
    public boolean deleteTicket(String visitId) {
        String sql = "DELETE FROM tickets WHERE visit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, visitId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error deleting ticket: " + visitId, e);
            return false;
        }
    }

    /**
     * Updates ticket status and assigns a doctor
     *
     * @param visitId Visit ID
     * @param status New status
     * @param doctorId Doctor ID (can be null)
     * @return true if successful, false otherwise
     */
    public boolean updateTicketStatusAndDoctor(String visitId, TicketStatus status, String doctorId) {
        String sql = "UPDATE tickets SET status = ?";
        
        if (doctorId != null) {
            sql += ", assigned_doctor_id = ?";
        }
        
        // Update timestamps based on status
        if (status == TicketStatus.CALLED || status == TicketStatus.IN_SERVICE) {
            sql += ", called_time = NOW()";
        } else if (status == TicketStatus.COMPLETED) {
            sql += ", completed_time = NOW(), wait_time_minutes = TIMESTAMPDIFF(MINUTE, created_time, NOW())";
        }
        
        sql += " WHERE visit_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, status.name());
            
            if (doctorId != null) {
                stmt.setString(paramIndex++, doctorId);
            }
            
            stmt.setString(paramIndex, visitId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating ticket status and doctor: " + visitId, e);
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a Ticket object
     * Creates patient from ResultSet data directly to avoid extra DB calls
     */
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        String visitId = rs.getString("visit_id");
        String ticketNumber = rs.getString("ticket_number");
        TicketStatus status = TicketStatus.valueOf(rs.getString("status"));
        String serviceType = rs.getString("service_type");
        String assignedDoctorId = rs.getString("assigned_doctor_id");
        String assignedDoctorName = rs.getString("doctor_name");

        // Parse priority
        PriorityLevel priority = PriorityLevel.REGULAR;
        try {
            String priorityStr = rs.getString("priority");
            if (priorityStr != null) {
                priority = PriorityLevel.valueOf(priorityStr);
            }
        } catch (IllegalArgumentException e) {
            // Default to REGULAR if invalid
        }

        Timestamp createdTimeTs = rs.getTimestamp("created_time");
        LocalDateTime createdTime = createdTimeTs != null ? createdTimeTs.toLocalDateTime() : LocalDateTime.now();

        Timestamp calledTimeTs = rs.getTimestamp("called_time");
        LocalDateTime calledTime = calledTimeTs != null ? calledTimeTs.toLocalDateTime() : null;

        // Create patient object directly from ResultSet (data is already joined)
        String patientId = rs.getString("patient_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String patientName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        patientName = patientName.trim();
        
        int age = 0;
        try {
            age = rs.getInt("age");
        } catch (SQLException e) {
            // Age might not be in result set
        }
        
        String phoneNumber = rs.getString("phone_number");
        boolean isSeniorCitizen = rs.getBoolean("is_senior_citizen");
        String chiefComplaint = rs.getString("chief_complaint");
        
        // Create a simplified Patient object with available data
        Patient patient = new Patient(
            patientId,
            patientName.isEmpty() ? "Unknown Patient" : patientName,
            age,
            phoneNumber,
            null, // homeAddress
            rs.getString("gender"),
            null, // emergencyContactPerson
            null, // emergencyContactNumber
            isSeniorCitizen,
            null, null, null, null, // medications, allergies, diagnosis, treatmentPlan
            chiefComplaint, // notes
            null, null, null, null, null, null, null, null // remaining fields
        );

        return new Ticket(visitId, ticketNumber, patient, status, priority, 
                         createdTime, calledTime, serviceType, assignedDoctorId, assignedDoctorName);
    }
}

