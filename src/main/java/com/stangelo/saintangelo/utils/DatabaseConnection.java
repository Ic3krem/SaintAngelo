package com.stangelo.saintangelo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Connection Utility Class
 * Manages MySQL database connections for the SaintAngelo Hospital Queue Management System
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class DatabaseConnection {

    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    // Database configuration - loaded from DatabaseConfig
    private static String getDbUrl() {
        return DatabaseConfig.getDatabaseUrl();
    }

    private static String getDbUsername() {
        return DatabaseConfig.getDatabaseUsername();
    }

    private static String getDbPassword() {
        return DatabaseConfig.getDatabasePassword();
    }

    private static String getDbDriver() {
        return DatabaseConfig.getDatabaseDriver();
    }

    // Connection pool settings
    private static Connection connection = null;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 1000;

    /**
     * Private constructor to prevent instantiation
     * This is a utility class with static methods only
     */
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Establishes a connection to the MySQL database
     * Uses singleton pattern to maintain a single connection instance
     *
     * @return Connection object if successful, null otherwise
     */
    public static Connection getConnection() {
        try {
            // Check if connection exists and is still valid
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        // Try to validate connection with a longer timeout for network connections
                        try {
                            // Use 5 seconds timeout for validation to account for network latency
                            if (connection.isValid(5)) {
                                // Additional check: try a simple query to ensure connection is really alive
                                try {
                                    connection.createStatement().execute("SELECT 1");
                                    return connection;
                                } catch (SQLException e) {
                                    // Connection appears valid but query fails - connection is stale
                                    logger.warning("Connection validation query failed, connection is stale: " + e.getMessage());
                                    connection = null;
                                }
                            } else {
                                logger.warning("Connection validation failed, connection is invalid");
                                connection = null;
                            }
                        } catch (AbstractMethodError | NoSuchMethodError e) {
                            // isValid() not available, try a simple query instead
                            try {
                                connection.createStatement().execute("SELECT 1");
                                return connection;
                            } catch (SQLException queryException) {
                                logger.warning("Connection test query failed: " + queryException.getMessage());
                                connection = null;
                            }
                        }
                    } else {
                        logger.info("Connection is closed, creating new one");
                        connection = null;
                    }
                } catch (SQLException e) {
                    // Connection is invalid, reset it
                    logger.warning("Existing connection is invalid, creating new one: " + e.getMessage());
                    connection = null;
                }
            }

            // Load MySQL JDBC driver
            try {
                Class.forName(getDbDriver());
                logger.info("JDBC Driver loaded: " + getDbDriver());
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "MySQL JDBC Driver not found. Make sure mysql-connector-j is in the classpath.", e);
                return null;
            }

            // Establish connection with retry logic
            connection = establishConnectionWithRetry();

            if (connection != null) {
                logger.info("Database connection established successfully");
            }

            return connection;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to establish database connection", e);
            connection = null; // Reset connection on failure
            return null;
        }
    }

    /**
     * Establishes connection with retry mechanism
     *
     * @return Connection object
     * @throws SQLException if connection fails after all retry attempts
     */
    private static Connection establishConnectionWithRetry() throws SQLException {
        SQLException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                String url = getDbUrl();
                String username = getDbUsername();
                String password = getDbPassword();
                
                logger.info(String.format("Attempting connection (attempt %d/%d): URL=%s, Username=%s", 
                    attempt, MAX_RETRY_ATTEMPTS, url, username));
                
                Connection conn = DriverManager.getConnection(url, username, password);

                // Set connection properties for better performance
                conn.setAutoCommit(true);

                logger.info("Database connection attempt " + attempt + " succeeded");
                return conn;

            } catch (SQLException e) {
                lastException = e;
                String errorMsg = String.format("Database connection attempt %d failed: %s\n" +
                        "URL: %s\nUsername: %s\nError Code: %d\nSQL State: %s",
                        attempt, e.getMessage(), getDbUrl(), getDbUsername(), 
                        e.getErrorCode(), e.getSQLState());
                logger.severe(errorMsg);
                
                // Print stack trace for debugging
                logger.log(Level.SEVERE, "Full exception details:", e);

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Connection retry interrupted", ie);
                    }
                }
            }
        }

        throw lastException != null ? lastException : new SQLException("Failed to establish connection after " + MAX_RETRY_ATTEMPTS + " attempts");
    }

    /**
     * Closes the database connection
     * Should be called when the application shuts down or connection is no longer needed
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.info("Database connection closed successfully");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }

    /**
     * Tests the database connection
     * Useful for checking if database is accessible
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        logger.info("Starting database connection test...");
        try {
            // Reset connection to force a fresh connection attempt
            connection = null;
            
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                // Test with a simple query
                try {
                    conn.createStatement().executeQuery("SELECT 1");
                    logger.info("Database connection test successful");
                    return true;
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Connection exists but query failed: " + e.getMessage(), e);
                    connection = null; // Reset invalid connection
                    return false;
                }
            } else {
                logger.severe("getConnection() returned null or closed connection");
                return false;
            }
        } catch (SQLException e) {
            String errorDetails = String.format(
                "Database connection test failed!\n" +
                "URL: %s\n" +
                "Username: %s\n" +
                "Error: %s\n" +
                "Error Code: %d\n" +
                "SQL State: %s",
                getDbUrl(), getDbUsername(), e.getMessage(), 
                e.getErrorCode(), e.getSQLState()
            );
            logger.log(Level.SEVERE, errorDetails, e);
            connection = null; // Reset connection on failure
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during connection test: " + e.getMessage(), e);
            connection = null;
        }
        return false;
    }

    /**
     * Gets the current database URL
     *
     * @return Database URL string
     */
    public static String getDatabaseUrl() {
        return getDbUrl();
    }

    /**
     * Gets the database name from the URL
     *
     * @return Database name
     */
    public static String getDatabaseName() {
        return "saintangelo_hospital";
    }

    /**
     * Checks if connection is active
     *
     * @return true if connection exists and is open, false otherwise
     */
    public static boolean isConnectionActive() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Resets the connection (closes and reopens)
     * Useful for reconnecting after connection loss
     *
     * @return true if reset successful, false otherwise
     */
    public static boolean resetConnection() {
        closeConnection();
        Connection newConnection = getConnection();
        return newConnection != null;
    }
}

