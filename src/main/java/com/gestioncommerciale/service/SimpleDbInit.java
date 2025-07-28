package com.gestioncommerciale.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Base64;
import java.security.MessageDigest;

/**
 * Simple database initializer that focuses on creating a working admin user
 */
public class SimpleDbInit {
    private static final String DATABASE_URL = "jdbc:sqlite:gestion_commerciale.db";
    
    public static void main(String[] args) {
        try {
            System.out.println("Initializing database with minimal setup...");
            
            // Create database connection
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("✓ Database connected");
                
                // Create users table
                String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom VARCHAR(255) NOT NULL,
                        prenom VARCHAR(255) NOT NULL,
                        login VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE', 'VIEWER')),
                        active BOOLEAN NOT NULL DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_login TIMESTAMP,
                        email VARCHAR(255),
                        telephone VARCHAR(255)
                    )
                """;
                
                stmt.execute(createUsersTable);
                System.out.println("✓ Users table created");
                
                // Hash the password "admin123"
                String password = "admin123";
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getBytes("UTF-8"));
                String hashedPassword = Base64.getEncoder().encodeToString(hash);
                
                System.out.println("Password hash for 'admin123': " + hashedPassword);
                
                // Insert admin user
                String insertUser = """
                    INSERT OR REPLACE INTO users (nom, prenom, login, password, role, email, telephone) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
                    pstmt.setString(1, "Administrateur");
                    pstmt.setString(2, "Système");
                    pstmt.setString(3, "admin");
                    pstmt.setString(4, hashedPassword);
                    pstmt.setString(5, "ADMIN");
                    pstmt.setString(6, "admin@gestioncommerciale.com");
                    pstmt.setString(7, "0612345678");
                    
                    pstmt.executeUpdate();
                    System.out.println("✓ Admin user created/updated");
                }
                
                // Verify user creation
                var rs = stmt.executeQuery("SELECT login, role FROM users WHERE login = 'admin'");
                if (rs.next()) {
                    System.out.println("✓ Verification: Admin user found");
                    System.out.println("  Login: " + rs.getString("login"));
                    System.out.println("  Role: " + rs.getString("role"));
                } else {
                    System.out.println("✗ Admin user not found!");
                }
                
                System.out.println("\n✓ Database initialization completed successfully!");
                System.out.println("You can now use login: admin, password: admin123");
                
            }
        } catch (Exception e) {
            System.err.println("✗ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
