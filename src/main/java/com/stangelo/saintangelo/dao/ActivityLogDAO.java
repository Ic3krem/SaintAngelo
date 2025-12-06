package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.models.ActivityLog;
import com.stangelo.saintangelo.models.ActivityType;
import com.stangelo.saintangelo.models.User;
import com.stangelo.saintangelo.models.UserRole;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ActivityLog entity
 */
public class ActivityLogDAO extends BaseDAO {

    public boolean create(ActivityLog log) {
        // Use database's NOW() to ensure correct timezone handling
        String sql = "INSERT INTO activity_logs (user_id, action, details, activity_type, ip_address, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, COALESCE(?, NOW()))";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, log.getUser() != null ? log.getUser().getId() : null);
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getDetails());
            stmt.setString(4, log.getActivityType().name());
            stmt.setString(5, log.getIpAddress());
            
            // Use database time if timestamp is null or very old, otherwise use provided timestamp
            if (log.getTimestamp() != null) {
                // Convert LocalDateTime to Timestamp using system default timezone
                Timestamp ts = Timestamp.valueOf(log.getTimestamp());
                stmt.setTimestamp(6, ts);
            } else {
                stmt.setTimestamp(6, null); // Will use NOW() from COALESCE
            }

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

            // First, collect all data from ResultSet into a list
            List<ActivityLogData> logDataList = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        // Extract all data from ResultSet BEFORE doing any other DB operations
                        int logId = rs.getInt("log_id");
                        String userId = rs.getString("user_id");
                        String action = rs.getString("action");
                        String details = rs.getString("details");
                        String activityTypeStr = rs.getString("activity_type");
                        String ipAddress = rs.getString("ip_address");
                        Timestamp timestampValue = rs.getTimestamp("timestamp");
                        
                        // Debug: Log activity type from database
                        System.out.println("findRecent - Extracting log_id " + logId + " - activity_type: '" + activityTypeStr + "'");
                        if (activityTypeStr == null || activityTypeStr.trim().isEmpty()) {
                            System.out.println("WARNING: Activity type is null/empty for log_id: " + logId);
                        }
                        
                        logDataList.add(new ActivityLogData(logId, userId, action, details, 
                                                           activityTypeStr, ipAddress, timestampValue));
                    } catch (Exception e) {
                        logError("Error extracting recent activity log row data", e);
                    }
                }
            }
            
            // Now map the data to ActivityLog objects (this can do DB lookups safely)
            for (ActivityLogData data : logDataList) {
                try {
                    ActivityLog log = mapDataToActivityLog(data);
                    if (log != null) {
                        logs.add(log);
                    }
                } catch (Exception e) {
                    logError("Error mapping recent activity log (log_id: " + data.logId + ")", e);
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

            int rowCount = 0;
            int successCount = 0;
            int errorCount = 0;

            // First, collect all data from ResultSet into a list
            List<ActivityLogData> logDataList = new ArrayList<>();
            while (rs.next()) {
                rowCount++;
                try {
                    // Extract all data from ResultSet BEFORE doing any other DB operations
                    int logId = rs.getInt("log_id");
                    String userId = rs.getString("user_id");
                    String action = rs.getString("action");
                    String details = rs.getString("details");
                    String activityTypeStr = rs.getString("activity_type");
                    String ipAddress = rs.getString("ip_address");
                    Timestamp timestampValue = rs.getTimestamp("timestamp");
                    
                    // Debug: Log activity type from database
                    System.out.println("Extracting log_id " + logId + " - activity_type from DB: '" + activityTypeStr + "'");
                    if (activityTypeStr == null || activityTypeStr.trim().isEmpty()) {
                        logger.warning("Activity type is null or empty for log_id: " + logId);
                        System.out.println("WARNING: Activity type is null/empty for log_id: " + logId);
                    }
                    
                    logDataList.add(new ActivityLogData(logId, userId, action, details, 
                                                       activityTypeStr, ipAddress, timestampValue));
                } catch (Exception e) {
                    errorCount++;
                    logError("Error extracting activity log row data", e);
                }
            }
            
            // Now map the data to ActivityLog objects (this can do DB lookups safely)
            for (ActivityLogData data : logDataList) {
                try {
                    ActivityLog log = mapDataToActivityLog(data);
                    if (log != null) {
                        logs.add(log);
                        successCount++;
                    } else {
                        errorCount++;
                        logger.warning("Mapped log is null for log_id: " + data.logId);
                    }
                } catch (Exception e) {
                    errorCount++;
                    logError("Error mapping activity log (log_id: " + data.logId + ")", e);
                }
            }
            
            logger.info("ActivityLogDAO.findAll() - Total rows: " + rowCount + 
                       ", Successfully mapped: " + successCount + 
                       ", Errors: " + errorCount);
        } catch (SQLException e) {
            logError("Error retrieving all activity logs", e);
            logger.severe("SQL Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }
    
    /**
     * Helper class to hold extracted ResultSet data
     */
    private static class ActivityLogData {
        final int logId;
        final String userId;
        final String action;
        final String details;
        final String activityTypeStr;
        final String ipAddress;
        final Timestamp timestampValue;
        
        ActivityLogData(int logId, String userId, String action, String details,
                      String activityTypeStr, String ipAddress, Timestamp timestampValue) {
            this.logId = logId;
            this.userId = userId;
            this.action = action;
            this.details = details;
            this.activityTypeStr = activityTypeStr;
            this.ipAddress = ipAddress;
            this.timestampValue = timestampValue;
        }
    }
    
    /**
     * Maps extracted data to ActivityLog object (can safely do DB lookups)
     */
    private ActivityLog mapDataToActivityLog(ActivityLogData data) {
        // Handle activity type with null safety
        ActivityType activityType = null;
        if (data.activityTypeStr != null && !data.activityTypeStr.trim().isEmpty()) {
            try {
                activityType = ActivityType.valueOf(data.activityTypeStr);
                logger.fine("Mapped activity type: " + activityType.name() + " for log_id: " + data.logId);
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown activity type: " + data.activityTypeStr + " for log_id: " + data.logId);
                // Try to find a matching enum value (case-insensitive)
                try {
                    for (ActivityType type : ActivityType.values()) {
                        if (type.name().equalsIgnoreCase(data.activityTypeStr.trim())) {
                            activityType = type;
                            logger.info("Found case-insensitive match for activity type: " + type.name());
                            break;
                        }
                    }
                } catch (Exception ex) {
                    // Ignore
                }
                // Default to LOGIN if still null
                if (activityType == null) {
                    activityType = ActivityType.LOGIN;
                    logger.warning("Defaulting to LOGIN for log_id: " + data.logId);
                }
            }
        } else {
            logger.warning("Activity type string is null or empty for log_id: " + data.logId + ", defaulting to LOGIN");
            activityType = ActivityType.LOGIN; // Default to LOGIN if null
        }
        
        LocalDateTime timestamp = data.timestampValue != null ? 
                data.timestampValue.toLocalDateTime() : null;

        // Now safe to do DB lookup since ResultSet is closed
        User user = data.userId != null ? new UserDAO().findById(data.userId) : null;

        ActivityLog log = new ActivityLog(data.logId, user, data.action, data.details, 
                              activityType, data.ipAddress, timestamp);
        
        // Debug: Verify activity type was set
        System.out.println("mapDataToActivityLog - log_id: " + data.logId + 
                          ", activityTypeStr: '" + data.activityTypeStr + 
                          "', mapped to: " + (activityType != null ? activityType.name() : "NULL"));
        if (log.getActivityType() == null) {
            logger.severe("ERROR: Activity type is still null after mapping for log_id: " + data.logId);
            System.err.println("ERROR: Activity type is NULL for log_id: " + data.logId);
        }
        
        return log;
    }

    /**
     * Gets the total count of activity logs in the database
     * @return total count of logs
     */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM activity_logs";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int count = rs.getInt("total");
                logger.info("Total activity logs in database: " + count);
                return count;
            }
        } catch (SQLException e) {
            logError("Error getting total count of activity logs", e);
        }
        
        return 0;
    }

    /**
     * Gets activity count by user role for the last 30 days
     * @param role UserRole to count activities for
     * @return number of activities performed by users with the given role
     */
    public int getActivityCountByRole(UserRole role) {
        if (role == null) {
            return 0;
        }

        String sql = "SELECT COUNT(*) as count " +
                     "FROM activity_logs al " +
                     "INNER JOIN users u ON al.user_id = u.user_id " +
                     "WHERE u.role = ? " +
                     "AND al.timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            logError("Error getting activity count by role: " + role, e);
        }

        return 0;
    }

    /**
     * Gets activity count for admin roles (ADMIN + SUPER_ADMIN) for the last 30 days
     * @return number of activities performed by admin users
     */
    public int getActivityCountForAdmins() {
        String sql = "SELECT COUNT(*) as count " +
                     "FROM activity_logs al " +
                     "INNER JOIN users u ON al.user_id = u.user_id " +
                     "WHERE (u.role = 'ADMIN' OR u.role = 'SUPER_ADMIN') " +
                     "AND al.timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logError("Error getting activity count for admins", e);
        }

        return 0;
    }

    private ActivityLog mapResultSetToActivityLog(ResultSet rs) throws SQLException {
        int logId = rs.getInt("log_id");
        String userId = rs.getString("user_id");
        String action = rs.getString("action");
        String details = rs.getString("details");
        
        // Handle activity type with null safety
        ActivityType activityType = null;
        String activityTypeStr = rs.getString("activity_type");
        if (activityTypeStr != null && !activityTypeStr.trim().isEmpty()) {
            try {
                activityType = ActivityType.valueOf(activityTypeStr);
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown activity type: " + activityTypeStr + " for log_id: " + logId);
                // Default to LOGIN if unknown
                activityType = ActivityType.LOGIN;
            }
        }
        
        String ipAddress = rs.getString("ip_address");
        
        // Handle timestamp with proper timezone conversion
        Timestamp timestampValue = rs.getTimestamp("timestamp");
        LocalDateTime timestamp = timestampValue != null ? timestampValue.toLocalDateTime() : null;

        User user = userId != null ? new UserDAO().findById(userId) : null;

        return new ActivityLog(logId, user, action, details, activityType, ipAddress, timestamp);
    }
}

