package com.stangelo.saintangelo.dao;

import com.stangelo.saintangelo.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base DAO class providing common database operations
 * All DAO classes should extend this base class
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public abstract class BaseDAO {

    protected static final Logger logger = Logger.getLogger(BaseDAO.class.getName());

    /**
     * Gets a database connection
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    protected Connection getConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Failed to establish database connection");
        }
        return conn;
    }

    /**
     * Closes a connection safely
     *
     * @param conn Connection to close
     */
    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }

    /**
     * Logs SQL exceptions
     *
     * @param message Error message
     * @param e SQLException
     */
    protected void logError(String message, SQLException e) {
        logger.log(Level.SEVERE, message, e);
    }
}

