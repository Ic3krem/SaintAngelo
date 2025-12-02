package com.stangelo.saintangelo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Configuration Utility
 * Loads database configuration from properties file or uses defaults
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class DatabaseConfig {

    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());

    private static final String CONFIG_FILE = "database.properties";
    private static Properties properties = new Properties();

    // Default configuration values (for shared LAN database)
    private static final String DEFAULT_DB_URL = "jdbc:mysql://192.168.100.25:3306/saintangelo_hospital";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "";
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        loadConfiguration();
    }

    /**
     * Loads database configuration from properties file
     * Falls back to defaults if file not found
     */
    private static void loadConfiguration() {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                logger.info("Database configuration loaded from " + CONFIG_FILE);
                logger.info("DB URL: " + properties.getProperty("db.url", "not set"));
                logger.info("DB Username: " + properties.getProperty("db.username", "not set"));
                logger.info("DB Password: " + (properties.getProperty("db.password", "").isEmpty() ? "(empty)" : "***"));
            } else {
                logger.warning("Configuration file '" + CONFIG_FILE + "' not found in classpath. Using default values.");
                setDefaultProperties();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading configuration file. Using defaults.", e);
            setDefaultProperties();
        }
    }

    /**
     * Sets default properties
     */
    private static void setDefaultProperties() {
        properties.setProperty("db.url", DEFAULT_DB_URL);
        properties.setProperty("db.username", DEFAULT_DB_USERNAME);
        properties.setProperty("db.password", DEFAULT_DB_PASSWORD);
        properties.setProperty("db.driver", DEFAULT_DB_DRIVER);
    }

    /**
     * Gets database URL from configuration
     *
     * @return Database URL
     */
    public static String getDatabaseUrl() {
        return properties.getProperty("db.url", DEFAULT_DB_URL);
    }

    /**
     * Gets database username from configuration
     *
     * @return Database username
     */
    public static String getDatabaseUsername() {
        return properties.getProperty("db.username", DEFAULT_DB_USERNAME);
    }

    /**
* Gets database password from configuration
*
* @return Database password
*/
public static String getDatabasePassword() {
return properties.getProperty("db.password", DEFAULT_DB_PASSWORD);
}

    /**
     * Gets database driver class name
     *
     * @return Driver class name
     */
    public static String getDatabaseDriver() {
        return properties.getProperty("db.driver", DEFAULT_DB_DRIVER);
    }

    /**
     * Reloads configuration from file
     */
    public static void reloadConfiguration() {
        loadConfiguration();
    }
}
