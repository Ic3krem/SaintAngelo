package com.stangelo.saintangelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                    // Extract all data from ResultSet BEFORE doing anything else
                    String userId = rs.getString("user_id");
                    String dbUsername = rs.getString("username");
                    String dbPassword = rs.getString("password");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    UserRole role = UserRole.valueOf(rs.getString("role"));
                    String permissions = rs.getString("permissions");
                    String status = rs.getString("status");
                    Timestamp lastActiveTs = rs.getTimestamp("last_active");
                    LocalDateTime lastActive = lastActiveTs != null ? lastActiveTs.toLocalDateTime() : null;
                    
                    // Create user object with extracted data
                    User user = new User(userId, dbUsername, dbPassword, fullName, email, role, permissions, status, lastActive);
                    
                    // Now update last active (after we've extracted all data)
                    updateLastActive(userId);
                    
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
     * Gets all users
     *
     * @return List of all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

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
     * Deletes a user by ID
     *
     * @param userId User ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error deleting user: " + userId, e);
            return false;
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

        return new User(userId, username, password, fullName, email, role, permissions, status, lastActive);
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
     * @param status Status filter (Active, Inactive, or null for all)
     * @return List of users with the specified status
     */
    public List<User> findByStatus(String status) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
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
            sql.append(" AND status = ?");
            params.add(status);
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
}

