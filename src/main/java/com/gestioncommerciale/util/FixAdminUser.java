package com.gestioncommerciale.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.security.MessageDigest;

/**
 * Direct database utility to ensure admin user exists
 */
public class FixAdminUser {
    private static final String DATABASE_URL = "jdbc:sqlite:gestion_commerciale.db";
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Fixing Admin User Database ===");
            
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite driver loaded");
            
            try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                System.out.println("✓ Database connected: " + DATABASE_URL);
                
                // Create users table if not exists
                createUsersTable(conn);
                
                // Remove any existing admin user
                removeExistingAdmin(conn);
                
                // Create new admin user
                createAdminUser(conn);
                
                // Verify creation
                verifyAdminUser(conn);
                
                System.out.println("\n✓ Admin user setup completed!");
                System.out.println("You can now login with:");
                System.out.println("  Username: admin");
                System.out.println("  Password: admin123");
                
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createUsersTable(Connection conn) throws Exception {
        String createTable = """
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
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
            System.out.println("✓ Users table created/verified");
        }
    }
    
    private static void removeExistingAdmin(Connection conn) throws Exception {
        String deleteSQL = "DELETE FROM users WHERE login = 'admin'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("✓ Removed " + deleted + " existing admin user(s)");
            }
        }
    }
    
    private static void createAdminUser(Connection conn) throws Exception {
        // Generate SHA-256 hash for "admin123"
        String password = "admin123";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        String hashedPassword = Base64.getEncoder().encodeToString(hash);
        
        System.out.println("Creating admin user with password hash: " + hashedPassword);
        
        String insertSQL = """
            INSERT INTO users (nom, prenom, login, password, role, email, telephone, active, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, "Administrateur");
            pstmt.setString(2, "Système");
            pstmt.setString(3, "admin");
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, "ADMIN");
            pstmt.setString(6, "admin@gestioncommerciale.com");
            pstmt.setString(7, "0612345678");
            pstmt.setBoolean(8, true);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✓ Admin user created, rows affected: " + rowsAffected);
        }
    }
    
    private static void verifyAdminUser(Connection conn) throws Exception {
        String selectSQL = "SELECT * FROM users WHERE login = 'admin'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                System.out.println("\n=== Admin User Verification ===");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("prenom") + " " + rs.getString("nom"));
                System.out.println("Login: " + rs.getString("login"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Active: " + rs.getBoolean("active"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Password Hash: " + rs.getString("password"));
                System.out.println("Created: " + rs.getString("created_at"));
                
                // Test password verification
                String storedHash = rs.getString("password");
                String testPassword = "admin123";
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(testPassword.getBytes("UTF-8"));
                String testHash = Base64.getEncoder().encodeToString(hash);
                
                System.out.println("\n=== Password Verification ===");
                System.out.println("Test password: " + testPassword);
                System.out.println("Generated hash: " + testHash);
                System.out.println("Stored hash: " + storedHash);
                System.out.println("Hashes match: " + testHash.equals(storedHash));
                
            } else {
                System.out.println("✗ Admin user not found after creation!");
            }
        }
        
        // Count total users
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            System.out.println("\nTotal users in database: " + rs.getInt(1));
        }
    }
}
