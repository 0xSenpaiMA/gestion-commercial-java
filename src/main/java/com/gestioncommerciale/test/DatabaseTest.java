package com.gestioncommerciale.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Simple test to verify SQLite database connectivity
 */
public class DatabaseTest {
    private static final String DATABASE_URL = "jdbc:sqlite:test_gestion_commerciale.db";
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing SQLite database connection...");
            
            // Explicitly load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite JDBC driver loaded");
            
            // Test basic connection
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("✓ Database connection successful");
                
                // Test table creation
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)");
                System.out.println("✓ Table creation successful");
                
                // Test data insertion
                stmt.execute("INSERT OR IGNORE INTO test_table (name) VALUES ('test')");
                System.out.println("✓ Data insertion successful");
                
                // Test data retrieval
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table");
                if (rs.next()) {
                    System.out.println("✓ Data retrieval successful, count: " + rs.getInt(1));
                }
                
                // Test admin user creation with proper hash
                stmt.execute("DROP TABLE IF EXISTS test_users");
                stmt.execute("CREATE TABLE test_users (id INTEGER PRIMARY KEY, login TEXT, password TEXT, role TEXT)");
                stmt.execute("INSERT INTO test_users (login, password, role) VALUES ('admin', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMIN')");
                
                ResultSet userRs = stmt.executeQuery("SELECT * FROM test_users WHERE login = 'admin'");
                if (userRs.next()) {
                    System.out.println("✓ Admin user created successfully");
                    System.out.println("  Login: " + userRs.getString("login"));
                    System.out.println("  Password hash: " + userRs.getString("password"));
                    System.out.println("  Role: " + userRs.getString("role"));
                }
                
                System.out.println("\n✓ All database tests passed!");
                
            }
        } catch (Exception e) {
            System.err.println("✗ Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
