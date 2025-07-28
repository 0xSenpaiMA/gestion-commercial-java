package com.gestioncommerciale.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestioncommerciale.service.DatabaseInitService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Database configuration and initialization
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static EntityManagerFactory entityManagerFactory;
    private static final String DATABASE_URL = "jdbc:sqlite:gestion_commerciale.db";
    
    public static void initialize() {
        try {
            // Create database if it doesn't exist
            createDatabaseIfNotExists();
            
            // Initialize JPA
            initializeJPA();
            
            // Initialize sample data
            DatabaseInitService.initializeSampleData();
            
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static void createDatabaseIfNotExists() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 Statement stmt = conn.createStatement()) {
                
                // Enable foreign keys
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Create users table if it doesn't exist
                String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom VARCHAR(255) NOT NULL,
                        prenom VARCHAR(255) NOT NULL,
                        login VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(255) NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_login TIMESTAMP,
                        email VARCHAR(255),
                        telephone VARCHAR(255)
                    )
                """;
                stmt.execute(createUsersTable);
                logger.info("Users table created/verified");
                
                // Check if admin user exists
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE login = 'admin'");
                rs.next();
                int adminCount = rs.getInt(1);
                
                if (adminCount == 0) {
                    // Create admin user directly with SQL
                    logger.info("Creating admin user via SQL...");
                    
                    // Hash for "admin123": JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=
                    String insertAdmin = """
                        INSERT INTO users (nom, prenom, login, password, role, email, telephone, active, created_at) 
                        VALUES ('Administrateur', 'Système', 'admin', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMIN', 'admin@gestioncommerciale.com', '0612345678', 1, datetime('now'))
                    """;
                    
                    stmt.execute(insertAdmin);
                    logger.info("✓ Admin user created via SQL");
                    
                    // Verify creation
                    var verifyRs = stmt.executeQuery("SELECT login, role FROM users WHERE login = 'admin'");
                    if (verifyRs.next()) {
                        logger.info("✓ Admin user verified: {} with role {}", verifyRs.getString("login"), verifyRs.getString("role"));
                    }
                } else {
                    logger.info("Admin user already exists (count: {})", adminCount);
                }
                
                logger.info("SQLite database created/connected successfully");
            }
        } catch (Exception e) {
            logger.error("Error creating database", e);
            throw new RuntimeException("Failed to create database", e);
        }
    }
    
    private static void initializeJPA() {
        try {
            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
            properties.put("jakarta.persistence.jdbc.url", DATABASE_URL);
            properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.show_sql", "false");
            properties.put("hibernate.format_sql", "true");
            
            entityManagerFactory = Persistence.createEntityManagerFactory("gestionCommercialePU", properties);
            
            // Test the connection
            EntityManager em = entityManagerFactory.createEntityManager();
            em.close();
            
            logger.info("JPA initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing JPA", e);
            throw new RuntimeException("Failed to initialize JPA", e);
        }
    }
    
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            initialize();
        }
        return entityManagerFactory.createEntityManager();
    }
    
    public static void shutdown() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            logger.info("Database connection closed");
        }
    }
}
