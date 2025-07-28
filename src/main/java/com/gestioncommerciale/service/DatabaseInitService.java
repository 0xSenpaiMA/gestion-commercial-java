package com.gestioncommerciale.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to initialize database with sample data
 */
public class DatabaseInitService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitService.class);
    private static final String DATABASE_URL = "jdbc:sqlite:gestion_commerciale.db";
    
    public static void initializeSampleData() {
        try {
            // Check if database already has data
            if (isDatabaseInitialized()) {
                logger.info("Database already initialized with sample data");
                return;
            }
            
            // Execute initialization script
            executeInitScript();
            logger.info("Database initialized with sample data successfully");
            
            // Ensure default admin user exists (fallback)
            ensureDefaultAdminUser();
            
        } catch (Exception e) {
            logger.error("Error initializing sample data", e);
            
            // Try to create default admin user even if script fails
            try {
                ensureDefaultAdminUser();
            } catch (Exception ex) {
                logger.error("Failed to create fallback admin user", ex);
            }
        }
    }
    
    private static boolean isDatabaseInitialized() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            
            // Check if users table exists and has data
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='users'");
            if (rs.next() && rs.getInt(1) > 0) {
                // Table exists, check if it has data
                var userRs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (userRs.next() && userRs.getInt(1) > 0) {
                    return true;
                }
            }
            return false;
            
        } catch (Exception e) {
            logger.debug("Database not yet initialized: {}", e.getMessage());
            return false;
        }
    }
    
    private static void executeInitScript() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            
            // Read initialization script
            InputStream inputStream = DatabaseInitService.class.getResourceAsStream("/init-database.sql");
            if (inputStream == null) {
                logger.warn("init-database.sql not found in resources");
                return;
            }
            
            String script;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                script = reader.lines().collect(Collectors.joining("\n"));
            }
            
            // Split script into individual statements and execute
            String[] statements = script.split(";");
            for (String sql : statements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty() && !trimmedSql.startsWith("--")) {
                    stmt.execute(trimmedSql);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute initialization script", e);
        }
    }
    
    /**
     * Ensures that a default admin user exists in the database
     */
    private static void ensureDefaultAdminUser() {
        try {
            // Use AuthService to initialize default user
            AuthService.initializeDefaultUser();
            logger.info("Default admin user ensured");
        } catch (Exception e) {
            logger.error("Failed to ensure default admin user", e);
        }
    }
}
