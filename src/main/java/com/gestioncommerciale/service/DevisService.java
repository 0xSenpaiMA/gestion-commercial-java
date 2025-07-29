package com.gestioncommerciale.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Devis;
import com.gestioncommerciale.model.LigneDevis;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

/**
 * Service for devis management operations
 */
public class DevisService {
    
    public List<Devis> getAllDevis() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT d FROM Devis d ORDER BY d.dateCreation DESC", Devis.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Devis> getDevisByClient(Client client) {
        if (client == null || client.getId() == null) {
            return List.of();
        }
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT d FROM Devis d WHERE d.client.id = :clientId ORDER BY d.dateCreation DESC", Devis.class);
            query.setParameter("clientId", client.getId());
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Devis> getDevisByStatut(Devis.StatutDevis statut) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT d FROM Devis d WHERE d.statut = :statut ORDER BY d.dateCreation DESC", Devis.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Devis findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(Devis.class, id);
        } finally {
            em.close();
        }
    }
    
    public Devis findByNumero(String numero) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT d FROM Devis d WHERE d.numero = :numero", Devis.class);
            query.setParameter("numero", numero);
            List<Devis> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    public Devis save(Devis devis) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            // Generate numero if not set
            if (devis.getNumero() == null || devis.getNumero().trim().isEmpty()) {
                devis.setNumero(generateNumero());
            }
            
            // Ensure totals are calculated
            devis.calculateTotals();
            
            Devis savedDevis;
            if (devis.getId() == null) {
                em.persist(devis);
                savedDevis = devis;
            } else {
                savedDevis = em.merge(devis);
            }
            
            transaction.commit();
            return savedDevis;
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du devis: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Devis devis) {
        if (devis == null || devis.getId() == null) {
            return;
        }
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            Devis managedDevis = em.find(Devis.class, devis.getId());
            if (managedDevis != null) {
                em.remove(managedDevis);
            }
            
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du devis: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void deleteById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            Devis devis = em.find(Devis.class, id);
            if (devis != null) {
                em.remove(devis);
            }
            
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du devis: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public boolean existsByNumero(String numero) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT COUNT(d) FROM Devis d WHERE d.numero = :numero");
            query.setParameter("numero", numero);
            Long count = (Long) query.getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
    
    private String generateNumero() {
        String prefix = "DEV";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT COUNT(d) FROM Devis d WHERE d.numero LIKE :pattern");
            query.setParameter("pattern", prefix + datePart + "%");
            Long count = (Long) query.getSingleResult();
            
            return String.format("%s%s%03d", prefix, datePart, count + 1);
        } finally {
            em.close();
        }
    }
    
    public LigneDevis addLigneToDevis(Devis devis, Article article, BigDecimal quantite, BigDecimal prixUnitaire, BigDecimal remise) {
        LigneDevis ligne = new LigneDevis();
        ligne.setArticle(article);
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(prixUnitaire);
        ligne.setRemise(remise != null ? remise : BigDecimal.ZERO);
        ligne.calculateTotal();
        
        devis.addLigne(ligne);
        return ligne;
    }
    
    public void removeLigneFromDevis(Devis devis, LigneDevis ligne) {
        devis.removeLigne(ligne);
    }
    
    public List<Article> getAllArticles() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT a FROM Article a ORDER BY a.designation", Article.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Client> getAllClients() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Client c WHERE c.type = 'CLIENT' ORDER BY c.nom, c.prenom", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Devis duplicateDevis(Devis originalDevis) {
        if (originalDevis == null) {
            return null;
        }
        
        Devis newDevis = new Devis();
        newDevis.setClient(originalDevis.getClient());
        newDevis.setObservations(originalDevis.getObservations());
        newDevis.setTauxTVA(originalDevis.getTauxTVA());
        newDevis.setDateValidite(originalDevis.getDateValidite());
        
        // Copy lines
        for (LigneDevis originalLigne : originalDevis.getLignes()) {
            LigneDevis newLigne = new LigneDevis();
            newLigne.setArticle(originalLigne.getArticle());
            newLigne.setQuantite(originalLigne.getQuantite());
            newLigne.setPrixUnitaire(originalLigne.getPrixUnitaire());
            newLigne.setRemise(originalLigne.getRemise());
            newLigne.setDescription(originalLigne.getDescription());
            newLigne.calculateTotal();
            
            newDevis.addLigne(newLigne);
        }
        
        return newDevis;
    }
}
