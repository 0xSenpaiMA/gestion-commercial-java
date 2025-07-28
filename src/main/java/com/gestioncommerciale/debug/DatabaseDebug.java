package com.gestioncommerciale.debug;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.security.MessageDigest;

/**
 * Direct database debug utility
 */
public class DatabaseDebug {
    private static final String DATABASE_URL = "jdbc:sqlite:gestion_commerciale.db";
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Database Debug Utility ===");
            
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite driver loaded");
            
            try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                System.out.println("✓ Database connected: " + DATABASE_URL);
                
                // Check if users table exists
                checkUsersTable(conn);
                
                // List all users
                listAllUsers(conn);
                
                // Create admin user if not exists
                createAdminUser(conn);
                
                // List users again
                System.out.println("\n--- After admin creation ---");
                listAllUsers(conn);
                
                // Test login
                testLogin(conn, "admin", "admin123");
                
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkUsersTable(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
            if (rs.next()) {
                System.out.println("✓ Users table exists");
                
                // Check table structure
                rs = stmt.executeQuery("PRAGMA table_info(users)");
                System.out.println("Table structure:");
                while (rs.next()) {
                    System.out.printf("  %s: %s%n", rs.getString("name"), rs.getString("type"));
                }
            } else {
                System.out.println("✗ Users table does not exist");
                // Create it
                createUsersTable(conn);
            }
        }
    }
    
    private static void createUsersTable(Connection conn) throws Exception {
        String createTable = """
            CREATE TABLE users (
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
            System.out.println("✓ Users table created");
        }
    }
    
    private static void listAllUsers(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            
            System.out.println("\n--- Users in database ---");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("User %d: %s %s (login: %s, role: %s, active: %s)%n",
                    rs.getInt("id"),
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("login"),
                    rs.getString("role"),
                    rs.getBoolean("active"));
            }
            
            if (count == 0) {
                System.out.println("No users found in database");
            } else {
                System.out.println("Total users: " + count);
            }
        }
    }
    
    private static void createAdminUser(Connection conn) throws Exception {
        // Check if admin exists
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE login = ?")) {
            pstmt.setString(1, "admin");
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            if (count > 0) {
                System.out.println("Admin user already exists");
                return;
            }
        }
        
        // Create admin user
        String password = "admin123";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        String hashedPassword = Base64.getEncoder().encodeToString(hash);
        
        System.out.println("Creating admin user with password hash: " + hashedPassword);
        
        String insertUser = """
            INSERT INTO users (nom, prenom, login, password, role, email, telephone, active) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
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
    
    private static void testLogin(Connection conn, String login, String password) throws Exception {
        System.out.println("\n--- Testing login: " + login + " ---");
        
        // Hash the input password
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        String hashedPassword = Base64.getEncoder().encodeToString(hash);
        
        System.out.println("Input password hash: " + hashedPassword);
        
        // Find user
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE login = ?")) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                String role = rs.getString("role");
                boolean active = rs.getBoolean("active");
                
                System.out.println("User found:");
                System.out.println("  Stored hash: " + storedHash);
                System.out.println("  Role: " + role);
                System.out.println("  Active: " + active);
                
                if (hashedPassword.equals(storedHash)) {
                    System.out.println("✓ Password match - Login would succeed!");
                } else {
                    System.out.println("✗ Password mismatch - Login would fail!");
                }
            } else {
                System.out.println("✗ User not found!");
            }
        }
    }
}
