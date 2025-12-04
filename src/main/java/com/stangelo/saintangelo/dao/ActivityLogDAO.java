package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.ActivityLog;
import com.stangelo.saintangelo.models.ActivityType;
import com.stangelo.saintangelo.models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ActivityLog entity
 */
public class ActivityLogDAO extends BaseDAO {

    public boolean create(ActivityLog log) {
        String sql = "INSERT INTO activity_logs (user_id, action, details, activity_type, ip_address, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, log.getUser() != null ? log.getUser().getId() : null);
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getDetails());
            stmt.setString(4, log.getActivityType().name());
            stmt.setString(5, log.getIpAddress());
            stmt.setTimestamp(6, Timestamp.valueOf(log.getTimestamp()));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating activity log", e);
            return false;
        }
    }

    public List<ActivityLog> findByUser(String userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT 100";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToActivityLog(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding activity logs by user: " + userId, e);
        }
        return logs;
    }

    public List<ActivityLog> findByType(ActivityType type) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE activity_type = ? ORDER BY timestamp DESC LIMIT 100";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToActivityLog(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding activity logs by type: " + type, e);
        }
        return logs;
    }

    /**
     * Returns the most recent activity logs up to the provided limit.
     *
     * @param limit maximum number of records to return
     * @return list of ActivityLog objects ordered by newest first
     */
    public List<ActivityLog> findRecent(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        if (limit <= 0) {
            return logs;
        }

        String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToActivityLog(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error retrieving recent activity logs", e);
        }

        return logs;
    }

    /**
     * Retrieves all activity logs ordered by newest first.
     *
     * @return list of all activity logs
     */
    public List<ActivityLog> findAll() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToActivityLog(rs));
            }
        } catch (SQLException e) {
            logError("Error retrieving all activity logs", e);
        }

        return logs;
    }

    private ActivityLog mapResultSetToActivityLog(ResultSet rs) throws SQLException {
        int logId = rs.getInt("log_id");
        String userId = rs.getString("user_id");
        String action = rs.getString("action");
        String details = rs.getString("details");
        ActivityType activityType = ActivityType.valueOf(rs.getString("activity_type"));
        String ipAddress = rs.getString("ip_address");
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

        User user = userId != null ? new UserDAO().findById(userId) : null;

        return new ActivityLog(logId, user, action, details, activityType, ipAddress, timestamp);
    }
}

