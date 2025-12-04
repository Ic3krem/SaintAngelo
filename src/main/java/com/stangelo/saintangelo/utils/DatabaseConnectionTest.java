package com.stangelo.saintangelo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Standalone database connection test utility
 * Run this to diagnose database connection issues
 */
public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        // Set up console logging to see all messages
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(handler);
        
        System.out.println("=== Database Connection Diagnostic Test ===\n");
        
        // Test 1: Check configuration loading
        System.out.println("1. Testing Configuration Loading...");
        String url = DatabaseConfig.getDatabaseUrl();
        String username = DatabaseConfig.getDatabaseUsername();
        String password = DatabaseConfig.getDatabasePassword();
        String driver = DatabaseConfig.getDatabaseDriver();
        
        System.out.println("   URL: " + url);
        System.out.println("   Username: " + username);
        System.out.println("   Password: " + (password.isEmpty() ? "(empty)" : "***"));
        System.out.println("   Driver: " + driver);
        System.out.println();
        
        // Test 2: Check driver class
        System.out.println("2. Testing JDBC Driver...");
        try {
            Class.forName(driver);
            System.out.println("   ✓ Driver class found: " + driver);
        } catch (ClassNotFoundException e) {
            System.out.println("   ✗ Driver class NOT found: " + driver);
            System.out.println("   Error: " + e.getMessage());
            System.out.println("   Make sure mysql-connector-j is in your classpath!");
            return;
        }
        System.out.println();
        
        // Test 3: Direct connection test
        System.out.println("3. Testing Direct Connection...");
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("   ✓ Connection successful!");
            System.out.println("   Connection class: " + conn.getClass().getName());
            System.out.println("   Auto-commit: " + conn.getAutoCommit());
            System.out.println("   Read-only: " + conn.isReadOnly());
            
            // Test query
            System.out.println("4. Testing Query Execution...");
            try {
                conn.createStatement().executeQuery("SELECT 1");
                System.out.println("   ✓ Query executed successfully!");
            } catch (SQLException e) {
                System.out.println("   ✗ Query failed: " + e.getMessage());
            }
            
            // Check database
            System.out.println("5. Checking Database...");
            try {
                conn.createStatement().executeQuery("USE saintangelo_hospital");
                System.out.println("   ✓ Database 'saintangelo_hospital' exists and accessible!");
                
                // Check users table
                try {
                    conn.createStatement().executeQuery("SELECT COUNT(*) FROM users");
                    System.out.println("   ✓ Table 'users' exists!");
                } catch (SQLException e) {
                    System.out.println("   ✗ Table 'users' not found or not accessible: " + e.getMessage());
                }
            } catch (SQLException e) {
                System.out.println("   ✗ Database 'saintangelo_hospital' not found or not accessible: " + e.getMessage());
            }
            
            conn.close();
            System.out.println("\n=== All Tests Passed! ===");
            
        } catch (SQLException e) {
            System.out.println("   ✗ Connection FAILED!");
            System.out.println("   Error Message: " + e.getMessage());
            System.out.println("   Error Code: " + e.getErrorCode());
            System.out.println("   SQL State: " + e.getSQLState());
            System.out.println();
            
            // Provide specific troubleshooting based on error code
            System.out.println("Troubleshooting:");
            if (e.getErrorCode() == 0) {
                System.out.println("   - Error code 0 usually means network/connection issue");
                System.out.println("   - Check if the server at 192.168.100.25 is reachable");
                System.out.println("   - Try: ping 192.168.100.25");
                System.out.println("   - Check firewall settings");
            } else if (e.getErrorCode() == 1045) {
                System.out.println("   - Error 1045: Access denied (wrong username/password)");
                System.out.println("   - Verify credentials in database.properties");
            } else if (e.getErrorCode() == 1049) {
                System.out.println("   - Error 1049: Unknown database");
                System.out.println("   - Database 'saintangelo_hospital' doesn't exist");
                System.out.println("   - Import SaintAngeloSchema.sql on the server");
            } else if (e.getErrorCode() == 2003) {
                System.out.println("   - Error 2003: Can't connect to MySQL server");
                System.out.println("   - Server might not be running");
                System.out.println("   - Port 3306 might be blocked");
            }
        }
        
        // Test 4: Test using DatabaseConnection utility
        System.out.println("\n6. Testing DatabaseConnection Utility...");
        boolean testResult = DatabaseConnection.testConnection();
        if (testResult) {
            System.out.println("   ✓ DatabaseConnection.testConnection() returned true");
        } else {
            System.out.println("   ✗ DatabaseConnection.testConnection() returned false");
        }
    }
}

