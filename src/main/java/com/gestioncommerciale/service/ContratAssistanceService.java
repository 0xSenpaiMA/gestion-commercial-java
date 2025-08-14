package com.gestioncommerciale.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.ContratAssistance;
import com.gestioncommerciale.model.Facture;

/**
 * Service class for ContratAssistance operations
 */
public class ContratAssistanceService {
    
    public List<ContratAssistance> getAllContrats() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<ContratAssistance> query = em.createQuery(
                "SELECT c FROM ContratAssistance c " +
                "LEFT JOIN FETCH c.client " +
                "ORDER BY c.dateDebut DESC", ContratAssistance.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<ContratAssistance> getContratsActifs() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            TypedQuery<ContratAssistance> query = em.createQuery(
                "SELECT c FROM ContratAssistance c " +
                "LEFT JOIN FETCH c.client " +
                "WHERE c.actif = true " +
                "AND c.dateDebut <= :today " +
                "AND c.dateFin >= :today " +
                "ORDER BY c.dateDebut DESC", ContratAssistance.class);
            query.setParameter("today", today);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public ContratAssistance getContratById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            ContratAssistance contrat = em.find(ContratAssistance.class, id);
            if (contrat != null) {
                // Force loading of lazy relationships
                if (contrat.getClient() != null) {
                    contrat.getClient().getNom();
                }
                contrat.getFactures().size();
            }
            return contrat;
        } finally {
            em.close();
        }
    }
    
    public ContratAssistance save(ContratAssistance contrat) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            // Generate numero if not set
            if (contrat.getNumero() == null || contrat.getNumero().trim().isEmpty()) {
                contrat.setNumero(generateNumero());
            }
            
            ContratAssistance savedContrat;
            if (contrat.getId() == null) {
                em.persist(contrat);
                savedContrat = contrat;
            } else {
                savedContrat = em.merge(contrat);
            }
            
            transaction.commit();
            return savedContrat;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du contrat d'assistance: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(ContratAssistance contrat) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            ContratAssistance managedContrat = em.find(ContratAssistance.class, contrat.getId());
            if (managedContrat != null) {
                // Remove association with factures before deleting
                for (Facture facture : managedContrat.getFactures()) {
                    facture.setContratAssistance(null);
                }
                managedContrat.getFactures().clear();
                
                em.remove(managedContrat);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du contrat d'assistance: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public List<ContratAssistance> getContratsByClient(Client client) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<ContratAssistance> query = em.createQuery(
                "SELECT c FROM ContratAssistance c " +
                "LEFT JOIN FETCH c.client " +
                "WHERE c.client = :client " +
                "ORDER BY c.dateDebut DESC", ContratAssistance.class);
            query.setParameter("client", client);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<ContratAssistance> getContratsExpires() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            TypedQuery<ContratAssistance> query = em.createQuery(
                "SELECT c FROM ContratAssistance c " +
                "LEFT JOIN FETCH c.client " +
                "WHERE c.dateFin < :today " +
                "ORDER BY c.dateFin DESC", ContratAssistance.class);
            query.setParameter("today", today);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<ContratAssistance> getContratsExpirantBientot(int nombreJours) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDate dateLimite = today.plusDays(nombreJours);
            TypedQuery<ContratAssistance> query = em.createQuery(
                "SELECT c FROM ContratAssistance c " +
                "LEFT JOIN FETCH c.client " +
                "WHERE c.actif = true " +
                "AND c.dateFin BETWEEN :today AND :dateLimite " +
                "ORDER BY c.dateFin ASC", ContratAssistance.class);
            query.setParameter("today", today);
            query.setParameter("dateLimite", dateLimite);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Client> getAllClients() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c " +
                "WHERE c.active = true " +
                "ORDER BY c.nom ASC", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Facture> getFacturesDisponibles() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "WHERE f.contratAssistance IS NULL " +
                "ORDER BY f.dateFacturation DESC", Facture.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public String generateNumero() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String prefix = "CA" + today;
            
            TypedQuery<String> query = em.createQuery(
                "SELECT c.numero FROM ContratAssistance c " +
                "WHERE c.numero LIKE :prefix " +
                "ORDER BY c.numero DESC", String.class);
            query.setParameter("prefix", prefix + "%");
            query.setMaxResults(1);
            
            List<String> results = query.getResultList();
            
            int nextNumber = 1;
            if (!results.isEmpty()) {
                String lastNumero = results.get(0);
                String numberPart = lastNumero.substring(prefix.length());
                try {
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            
            return prefix + String.format("%03d", nextNumber);
        } finally {
            em.close();
        }
    }
    
    public long getContratCount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM ContratAssistance c", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public long getContratActifCount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM ContratAssistance c " +
                "WHERE c.actif = true " +
                "AND c.dateDebut <= :today " +
                "AND c.dateFin >= :today", Long.class);
            query.setParameter("today", today);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
