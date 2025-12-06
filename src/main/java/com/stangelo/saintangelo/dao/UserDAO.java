package com.stangelo.saintangelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDate;

import com.stangelo.saintangelo.models.User;
import com.stangelo.saintangelo.models.UserRole;

/**
 * Data Access Object for User entity
 * Handles all database operations related to users
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class UserDAO extends BaseDAO {

    /**
     * Authenticates a user by username and password
     *
     * @param username Username
     * @param password Password (plain text)
     * @return User object if authenticated, null otherwise
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'Active'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Map ResultSet to User object
                    User user = mapResultSetToUser(rs);
                    
                    // Update last active timestamp
                    // This will trigger the database trigger to create activity log
                    updateLastActive(user.getId());
                    
                    // Also manually create activity log as backup (in case trigger doesn't fire)
                    createLoginActivityLog(user);
                    
                    logger.info("User authenticated successfully: " + username);
                    return user;
                } else {
                    logger.warning("Authentication failed for user: " + username);
                }
            }
        } catch (SQLException e) {
            logError("Error authenticating user: " + username, e);
        }
        return null;
    }

    /**
     * Finds a user by username
     *
     * @param username Username
     * @return User object if found, null otherwise
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding user by username: " + username, e);
        }
        return null;
    }

    /**
     * Finds a user by user ID
     *
     * @param userId User ID
     * @return User object if found, null otherwise
     */
    public User findById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding user by ID: " + userId, e);
        }
        return null;
    }

    /**
     * Finds a user by email
     *
     * @param email Email address
     * @return User object if found, null otherwise
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding user by email: " + email, e);
        }
        return null;
    }

    /**
     * Gets all users (excluding archived users)
     *
     * @return List of all active and inactive users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status != 'Archived' ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logError("Error finding all users", e);
        }
        return users;
    }

    /**
     * Gets users by role
     *
     * @param role User role
     * @return List of users with the specified role
     */
    public List<User> findByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding users by role: " + role, e);
        }
        return users;
    }

    /**
     * Creates a new user
     *
     * @param user User object to create
     * @param email Email address (optional)
     * @param permissions Permissions description (optional)
     * @return true if successful, false otherwise
     */
    public boolean create(User user, String email, String permissions) {
        String sql = "INSERT INTO users (user_id, username, password, full_name, email, role, permissions, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'Active')";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, email != null ? email : "");
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, permissions != null ? permissions : "");

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating user: " + user.getUsername(), e);
            return false;
        }
    }

    /**
     * Updates an existing user
     *
     * @param user User object with updated information
     * @param email Email address (optional)
     * @param permissions Permissions description (optional)
     * @param status Status (optional)
     * @return true if successful, false otherwise
     */
    public boolean update(User user, String email, String permissions, String status) {
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ?, " +
                "role = ?, permissions = ?, status = ? WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, email != null ? email : "");
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, permissions != null ? permissions : "");
            stmt.setString(7, status != null ? status : "Active");
            stmt.setString(8, user.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating user: " + user.getId(), e);
            return false;
        }
    }

    /**
     * Archives a user by ID (moves to archive instead of deleting)
     *
     * @param userId User ID to archive
     * @return true if successful, false otherwise
     */
    public boolean archive(String userId) {
        String sql = "UPDATE users SET status = 'Archived', archived_at = NOW() WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error archiving user: " + userId, e);
            return false;
        }
    }

    /**
     * Deletes a user by ID (legacy method - now archives instead)
     * This method is kept for backward compatibility but now archives users
     *
     * @param userId User ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String userId) {
        // Archive instead of delete
        return archive(userId);
    }

    /**
     * Permanently deletes a user from the database
     * This should only be used for archived users
     *
     * @param userId User ID to permanently delete
     * @return true if successful, false otherwise
     */
    public boolean permanentlyDelete(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error permanently deleting user: " + userId, e);
            return false;
        }
    }

    /**
     * Finds all archived users
     *
     * @return List of archived users
     */
    public List<User> findArchivedUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'Archived' ORDER BY archived_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logError("Error finding archived users", e);
        }
        return users;
    }

    /**
     * Restores an archived user back to active status
     *
     * @param userId User ID to restore
     * @return true if successful, false otherwise
     */
    public boolean restoreUser(String userId) {
        String sql = "UPDATE users SET status = 'Active', archived_at = NULL WHERE user_id = ? AND status = 'Archived'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error restoring user: " + userId, e);
            return false;
        }
    }

    /**
     * Automatically deletes users that have been archived for 30 or more days
     *
     * @return Number of users deleted
     */
    public int autoDeleteOldArchivedUsers() {
        String sql = "DELETE FROM users WHERE status = 'Archived' AND archived_at < DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Auto-deleted " + rowsAffected + " users archived for 30+ days");
            }
            return rowsAffected;

        } catch (SQLException e) {
            logError("Error auto-deleting old archived users", e);
            return 0;
        }
    }

    /**
     * Updates the last active timestamp for a user
     *
     * @param userId User ID
     */
    public void updateLastActive(String userId) {
        String sql = "UPDATE users SET last_active = NOW() WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logError("Error updating last active for user: " + userId, e);
        }
    }
    
    /**
     * Creates an activity log entry for user login
     * This is a backup method in case the database trigger doesn't fire
     *
     * @param user User who logged in
     */
    private void createLoginActivityLog(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        
        // Check if recent login log exists, if not create one
        String checkSql = "SELECT COUNT(*) as count FROM activity_logs " +
                          "WHERE user_id = ? AND activity_type = 'LOGIN' " +
                          "AND timestamp > DATE_SUB(NOW(), INTERVAL 1 MINUTE)";
        
        try (Connection conn = getConnection()) {
            // Check if a login log was created in the last minute (by trigger or previous call)
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        // Login log already exists (probably created by trigger)
                        logger.info("Login activity log already exists for user: " + user.getId());
                        return;
                    }
                }
            }
            
            // Create login log if it doesn't exist
            String insertSql = "INSERT INTO activity_logs (user_id, action, details, activity_type, timestamp) " +
                              "VALUES (?, 'Login', CONCAT('User ', ?, ' logged in'), 'LOGIN', NOW())";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, user.getId());
                stmt.setString(2, user.getFullName());
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Created login activity log for user: " + user.getId() + " (" + user.getFullName() + ")");
                }
            }
            
        } catch (SQLException e) {
            // Log error but don't fail authentication
            logError("Error creating login activity log for user: " + user.getId(), e);
        }
    }

    /**
     * Maps a ResultSet row to a User object
     *
     * @param rs ResultSet
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        String permissions = rs.getString("permissions");
        String status = rs.getString("status");
        Timestamp lastActiveTs = rs.getTimestamp("last_active");
        LocalDateTime lastActive = lastActiveTs != null ? lastActiveTs.toLocalDateTime() : null;
        Timestamp archivedAtTs = rs.getTimestamp("archived_at");
        LocalDateTime archivedAt = archivedAtTs != null ? archivedAtTs.toLocalDateTime() : null;
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

        return new User(userId, username, password, fullName, email, role, permissions, status, lastActive, archivedAt, createdAt);
    }

    /**
     * Searches users by user ID or name
     *
     * @param searchTerm Search term (user ID or name)
     * @return List of matching users
     */
    public List<User> search(String searchTerm) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_id LIKE ? OR full_name LIKE ? OR username LIKE ? ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error searching users: " + searchTerm, e);
        }
        return users;
    }

    /**
     * Gets users filtered by status
     *
     * @param status Status filter (Active, Inactive, Archived, or null for all non-archived)
     * @return List of users with the specified status
     */
    public List<User> findByStatus(String status) {
        List<User> users = new ArrayList<>();
        String sql;
        
        if (status != null && status.equals("Archived")) {
            // If looking for archived, include them
            sql = "SELECT * FROM users WHERE status = ? ORDER BY created_at DESC";
        } else if (status != null) {
            // For other statuses, exclude archived
            sql = "SELECT * FROM users WHERE status = ? AND status != 'Archived' ORDER BY created_at DESC";
        } else {
            // Default: exclude archived
            sql = "SELECT * FROM users WHERE status != 'Archived' ORDER BY created_at DESC";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (status != null) {
                stmt.setString(1, status);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding users by status: " + status, e);
        }
        return users;
    }

    /**
     * Gets users with search and status filter
     *
     * @param searchTerm Search term (optional)
     * @param status Status filter (optional)
     * @return List of filtered users
     */
    public List<User> searchWithFilters(String searchTerm, String status) {
        List<User> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<String> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (user_id LIKE ? OR full_name LIKE ? OR username LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        
        if (status != null && !status.equals("All Status") && !status.isEmpty()) {
            if (status.equals("Archived")) {
                sql.append(" AND status = 'Archived'");
            } else {
                // For non-archived statuses, exclude archived users
                sql.append(" AND status = ? AND status != 'Archived'");
                params.add(status);
            }
        } else {
            // Default: exclude archived users unless specifically searching for them
            sql.append(" AND status != 'Archived'");
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error searching users with filters", e);
        }
        return users;
    }

    /**
     * Gets total number of users in the system.
     *
     * @return total user count
     */
    public int countAllUsers() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("Error counting all users", e);
        }
        return 0;
    }

    /**
     * Counts users by role.
     *
     * @param role UserRole to count
     * @return number of users with the given role
     */
    public int countByRole(UserRole role) {
        if (role == null) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("Error counting users by role: " + role, e);
        }

        return 0;
    }

    /**
     * Gets daily user registration counts for the last N days.
     *
     * @param days The number of days to look back.
     * @return A map where the key is the date and the value is the count of new users.
     */
    public Map<LocalDate, Integer> getDailyUserCounts(int days) {
        Map<LocalDate, Integer> userCounts = new LinkedHashMap<>();
        String sql = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                     "FROM users " +
                     "WHERE created_at >= CURDATE() - INTERVAL ? DAY " +
                     "GROUP BY DATE(created_at) " +
                     "ORDER BY DATE(created_at)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days - 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    int count = rs.getInt("count");
                    userCounts.put(date, count);
                }
            }
        } catch (SQLException e) {
            logError("Error getting daily user counts", e);
        }

        // Fill in missing days with 0
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            userCounts.putIfAbsent(date, 0);
        }

        return userCounts;
    }

    /**
     * Gets the total number of users created in a given period.
     *
     * @param start The start date of the period.
     * @param end The end date of the period.
     * @return The total number of users created.
     */
    public int getUserCountInPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM users WHERE created_at BETWEEN ? AND ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, start);
            stmt.setObject(2, end);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("Error getting user count in period", e);
        }
        return 0;
    }
}
