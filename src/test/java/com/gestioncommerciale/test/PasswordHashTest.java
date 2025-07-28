package com.gestioncommerciale.test;

import com.gestioncommerciale.service.AuthService;

/**
 * Simple test to verify password hashing
 */
public class PasswordHashTest {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = AuthService.hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Test verification
        boolean isValid = AuthService.verifyPassword(password, hash);
        System.out.println("Verification: " + isValid);
        
        // Test with database hash
        String dbHash = "jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=";
        boolean isDbValid = AuthService.verifyPassword(password, dbHash);
        System.out.println("Database hash verification: " + isDbValid);
    }
}
