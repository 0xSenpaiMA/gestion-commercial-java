package com.gestioncommerciale.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

/**
 * Authentication service for user login and management
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static User currentUser = null;
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static User authenticate(String login, String password) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            logger.info("=== Authentication attempt ===");
            logger.info("Login: '{}'", login);
            logger.info("Password length: {}", password != null ? password.length() : 0);
            
            // Trim whitespace from login
            String trimmedLogin = login != null ? login.trim() : "";
            
            if (trimmedLogin.isEmpty()) {
                logger.warn("Empty login provided");
                return null;
            }
            
            if (password == null || password.isEmpty()) {
                logger.warn("Empty password provided");
                return null;
            }
            
            // First, check if user exists at all
            Query checkQuery = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class);
            checkQuery.setParameter("login", trimmedLogin);
            Long userCount = (Long) checkQuery.getSingleResult();
            logger.info("Users found with login '{}': {}", trimmedLogin, userCount);
            
            if (userCount == 0) {
                logger.warn("No user found with login: '{}'", trimmedLogin);
                // List all users for debugging
                listAllUsersForDebug(em);
                return null;
            }
            
            // Now get the user
            Query query = em.createQuery(
                "SELECT u FROM User u WHERE u.login = :login", User.class);
            query.setParameter("login", trimmedLogin);
            
            User user = (User) query.getSingleResult();
            logger.info("User found: {} {} (login: {}, role: {}, active: {})", 
                user.getPrenom(), user.getNom(), user.getLogin(), user.getRole(), user.isActive());
            
            if (!user.isActive()) {
                logger.warn("User account is inactive: {}", trimmedLogin);
                return null;
            }
            
            // Verify password
            String hashedInputPassword = hashPassword(password);
            String storedPassword = user.getPassword();
            
            logger.debug("Input password hash: {}", hashedInputPassword);
            logger.debug("Stored password hash: {}", storedPassword);
            logger.debug("Hashes match: {}", hashedInputPassword.equals(storedPassword));
            
            if (verifyPassword(password, storedPassword)) {
                // Update last login
                em.getTransaction().begin();
                user.setLastLogin(LocalDateTime.now().toString());
                em.merge(user);
                em.getTransaction().commit();
                
                currentUser = user;
                logger.info("✓ Authentication successful for user: {}", trimmedLogin);
                return user;
            } else {
                logger.warn("✗ Invalid password for user: {}", trimmedLogin);
                return null;
            }
            
        } catch (NoResultException e) {
            logger.warn("User not found: {}", login);
            return null;
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", login, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    private static void listAllUsersForDebug(EntityManager em) {
        try {
            Query debugQuery = em.createQuery("SELECT u FROM User u");
            @SuppressWarnings("unchecked")
            java.util.List<User> allUsers = debugQuery.getResultList();
            
            logger.info("=== All users in database (for debugging) ===");
            if (allUsers.isEmpty()) {
                logger.info("No users found in database!");
            } else {
                for (User u : allUsers) {
                    logger.info("User: {} {} (login: '{}', role: {}, active: {})", 
                        u.getPrenom(), u.getNom(), u.getLogin(), u.getRole(), u.isActive());
                }
            }
            logger.info("=== End user list ===");
        } catch (Exception e) {
            logger.error("Error listing users for debug", e);
        }
    }
    
    public static void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getLogin());
            currentUser = null;
        }
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public static boolean verifyPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }
    
    public static boolean hasPermission(User.UserRole requiredRole) {
        if (currentUser == null) {
            return false;
        }
        
        // Admin has all permissions
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // Check role hierarchy
        switch (requiredRole) {
            case VIEWER:
                return true; // All logged-in users can view
            case EMPLOYEE:
                return currentUser.getRole() == User.UserRole.EMPLOYEE ||
                       currentUser.getRole() == User.UserRole.MANAGER;
            case MANAGER:
                return currentUser.getRole() == User.UserRole.MANAGER;
            case ADMIN:
                return currentUser.getRole() == User.UserRole.ADMIN;
            default:
                return false;
        }
    }
    
    public static void initializeDefaultUser() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            logger.info("=== Initializing default admin user ===");
            
            // First, check if admin user already exists
            Query checkQuery = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class);
            checkQuery.setParameter("login", "admin");
            Long existingCount = (Long) checkQuery.getSingleResult();
            
            logger.info("Existing admin users count: {}", existingCount);
            
            if (existingCount > 0) {
                // User exists, verify it's configured correctly
                Query userQuery = em.createQuery(
                    "SELECT u FROM User u WHERE u.login = :login", User.class);
                userQuery.setParameter("login", "admin");
                User existingAdmin = (User) userQuery.getSingleResult();
                
                logger.info("Admin user exists: {} {} (role: {}, active: {})",
                    existingAdmin.getPrenom(), existingAdmin.getNom(), 
                    existingAdmin.getRole(), existingAdmin.isActive());
                
                // Ensure the password is correct (re-hash admin123)
                String correctHash = hashPassword("admin123");
                if (!correctHash.equals(existingAdmin.getPassword())) {
                    logger.info("Updating admin password to correct hash");
                    em.getTransaction().begin();
                    existingAdmin.setPassword(correctHash);
                    existingAdmin.setActive(true);
                    em.merge(existingAdmin);
                    em.getTransaction().commit();
                    logger.info("✓ Admin password updated");
                }
                
                return;
            }
            
            logger.info("No admin user found, creating new one...");
            
            // Create new admin user
            em.getTransaction().begin();
            
            User admin = new User();
            admin.setNom("Administrateur");
            admin.setPrenom("Système");
            admin.setLogin("admin");
            admin.setPassword(hashPassword("admin123"));
            admin.setRole(User.UserRole.ADMIN);
            admin.setEmail("admin@gestioncommerciale.com");
            admin.setTelephone("0612345678");
            admin.setActive(true);
            
            em.persist(admin);
            em.getTransaction().commit();
            
            logger.info("✓ Default admin user created successfully!");
            logger.info("  Login: admin");
            logger.info("  Password: admin123");
            logger.info("  Role: ADMIN");
            logger.info("  Password hash: {}", admin.getPassword());
            
            // Verify creation
            Query verifyQuery = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class);
            verifyQuery.setParameter("login", "admin");
            Long verifyCount = (Long) verifyQuery.getSingleResult();
            logger.info("Verification: {} users with login 'admin'", verifyCount);
            
            if (verifyCount == 0) {
                logger.error("✗ Admin user creation failed - user not found after creation!");
            }
            
        } catch (Exception e) {
            logger.error("Error initializing default user", e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }
    
    /**
     * List all users in the database for debugging
     */
    public static void listAllUsers() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT u FROM User u");
            @SuppressWarnings("unchecked")
            java.util.List<User> users = query.getResultList();
            
            logger.info("=== All users in database ===");
            logger.info("Total users: {}", users.size());
            
            for (User user : users) {
                logger.info("User: {} {} (login: {}, role: {}, active: {})", 
                    user.getPrenom(), user.getNom(), user.getLogin(), 
                    user.getRole(), user.isActive());
            }
            logger.info("=== End user list ===");
            
        } catch (Exception e) {
            logger.error("Error listing users", e);
        } finally {
            em.close();
        }
    }
}
