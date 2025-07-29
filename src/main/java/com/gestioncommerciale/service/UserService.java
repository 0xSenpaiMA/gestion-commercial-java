package com.gestioncommerciale.service;

import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

/**
 * Service for user management operations
 */
public class UserService {
    
    public List<User> getAllUsers() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT u FROM User u ORDER BY u.nom, u.prenom", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<User> getActiveUsers() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT u FROM User u WHERE u.active = true ORDER BY u.nom, u.prenom", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public User findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
    
    public User findByLogin(String login) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class);
            query.setParameter("login", login);
            return (User) query.getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
    
    public User save(User user) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            User savedUser;
            if (user.getId() == null) {
                // New user - ensure createdAt is set
                if (user.getCreatedAt() == null) {
                    user.setCreatedAt(java.time.LocalDateTime.now().toString());
                }
                em.persist(user);
                savedUser = user;
            } else {
                savedUser = em.merge(user);
            }
            
            em.flush(); // Force immediate write to database
            transaction.commit();
            return savedUser;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    // Log but don't throw rollback exception
                }
            }
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            User user = em.find(User.class, id);
            if (user != null) {
                // Don't actually delete, just deactivate
                user.setActive(false);
                em.merge(user);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting user", e);
        } finally {
            em.close();
        }
    }
    
    public boolean isLoginUnique(String login, Long excludeId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String jpql = "SELECT COUNT(u) FROM User u WHERE u.login = :login";
            if (excludeId != null) {
                jpql += " AND u.id != :excludeId";
            }
            
            Query query = em.createQuery(jpql, Long.class);
            query.setParameter("login", login);
            if (excludeId != null) {
                query.setParameter("excludeId", excludeId);
            }
            
            Long count = (Long) query.getSingleResult();
            return count == 0;
        } finally {
            em.close();
        }
    }
}
