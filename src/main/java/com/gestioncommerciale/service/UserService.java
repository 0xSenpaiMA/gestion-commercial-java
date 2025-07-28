package com.gestioncommerciale.service;

import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.User;

import jakarta.persistence.EntityManager;
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
        try {
            em.getTransaction().begin();
            
            if (user.getId() == null) {
                em.persist(user);
            } else {
                user = em.merge(user);
            }
            
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving user", e);
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
