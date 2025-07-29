package com.gestioncommerciale.service;

import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Client;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

/**
 * Service for client management operations
 */
public class ClientService {
    
    public List<Client> getAllClients() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Client c ORDER BY c.nom, c.prenom", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Client> getActiveClients() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Client c WHERE c.active = true ORDER BY c.nom, c.prenom", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Client> getClientsByType(Client.ClientType type) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Client c WHERE c.type = :type AND c.active = true ORDER BY c.nom, c.prenom", Client.class);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Client findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(Client.class, id);
        } finally {
            em.close();
        }
    }
    
    public Client findByNom(String nom) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Client c WHERE c.nom = :nom", Client.class);
            query.setParameter("nom", nom);
            List<Client> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    public Client save(Client client) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            if (client.getId() == null) {
                // New client
                if (client.getCreatedAt() == null) {
                    client.setCreatedAt(java.time.LocalDateTime.now().toString());
                }
                em.persist(client);
            } else {
                // Update existing client
                client = em.merge(client);
            }
            
            transaction.commit();
            return client;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving client: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            Client client = em.find(Client.class, id);
            if (client != null) {
                em.remove(client);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting client: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void deactivate(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            Client client = em.find(Client.class, id);
            if (client != null) {
                client.setActive(false);
                em.merge(client);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deactivating client: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public long getClientsCount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT COUNT(c) FROM Client c WHERE c.active = true");
            return (Long) query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public long getClientsByTypeCount(Client.ClientType type) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT COUNT(c) FROM Client c WHERE c.type = :type AND c.active = true");
            query.setParameter("type", type);
            return (Long) query.getSingleResult();
        } finally {
            em.close();
        }
    }
}