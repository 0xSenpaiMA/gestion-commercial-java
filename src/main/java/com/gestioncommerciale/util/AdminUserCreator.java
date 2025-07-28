package com.gestioncommerciale.util;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.User;
import com.gestioncommerciale.service.AuthService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to create admin user directly
 */
public class AdminUserCreator {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserCreator.class);
    
    public static void createAdminUser() {
        try {
            logger.info("Creating admin user...");
            
            EntityManager em = DatabaseConfig.getEntityManager();
            
            try {
                // First, check if admin user already exists
                Query query = em.createQuery("SELECT u FROM User u WHERE u.login = :login");
                query.setParameter("login", "admin");
                
                try {
                    User existingUser = (User) query.getSingleResult();
                    logger.info("Admin user already exists: {}", existingUser.getLogin());
                    return;
                } catch (Exception e) {
                    // User doesn't exist, create it
                    logger.info("Admin user doesn't exist, creating...");
                }
                
                // Create admin user
                em.getTransaction().begin();
                
                User admin = new User();
                admin.setNom("Administrateur");
                admin.setPrenom("Système");
                admin.setLogin("admin");
                admin.setPassword(AuthService.hashPassword("admin123"));
                admin.setRole(User.UserRole.ADMIN);
                admin.setEmail("admin@gestioncommerciale.com");
                admin.setTelephone("0612345678");
                admin.setActive(true);
                
                em.persist(admin);
                em.getTransaction().commit();
                
                logger.info("✓ Admin user created successfully!");
                logger.info("  Login: admin");
                logger.info("  Password: admin123");
                logger.info("  Role: ADMIN");
                
                // Verify creation
                Query verifyQuery = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class);
                verifyQuery.setParameter("login", "admin");
                Long count = (Long) verifyQuery.getSingleResult();
                
                if (count > 0) {
                    logger.info("✓ Verification: Admin user found in database");
                } else {
                    logger.error("✗ Verification failed: Admin user not found in database");
                }
                
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            logger.error("Failed to create admin user", e);
        }
    }
    
    public static void main(String[] args) {
        try {
            // Initialize database first
            DatabaseConfig.initialize();
            
            // Create admin user
            createAdminUser();
            
            System.out.println("Admin user creation completed. You can now login with admin/admin123");
            
        } catch (Exception e) {
            System.err.println("Failed to create admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
