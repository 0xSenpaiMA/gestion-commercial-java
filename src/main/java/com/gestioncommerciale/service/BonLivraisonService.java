package com.gestioncommerciale.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.BonLivraison;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Devis;
import com.gestioncommerciale.model.LigneLivraison;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

/**
 * Service for bon de livraison management operations
 */
public class BonLivraisonService {
    
    public List<BonLivraison> getAllBonsLivraison() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT bl FROM BonLivraison bl ORDER BY bl.dateCreation DESC", BonLivraison.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<BonLivraison> getBonsLivraisonByClient(Client client) {
        if (client == null || client.getId() == null) {
            return List.of();
        }
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT bl FROM BonLivraison bl WHERE bl.client.id = :clientId ORDER BY bl.dateCreation DESC", BonLivraison.class);
            query.setParameter("clientId", client.getId());
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<BonLivraison> getBonsLivraisonByStatut(BonLivraison.StatutLivraison statut) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT bl FROM BonLivraison bl WHERE bl.statut = :statut ORDER BY bl.dateCreation DESC", BonLivraison.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public BonLivraison findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(BonLivraison.class, id);
        } finally {
            em.close();
        }
    }
    
    public BonLivraison findByNumero(String numero) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT bl FROM BonLivraison bl WHERE bl.numero = :numero", BonLivraison.class);
            query.setParameter("numero", numero);
            List<BonLivraison> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    public BonLivraison save(BonLivraison bonLivraison) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            // Generate numero if not set
            if (bonLivraison.getNumero() == null || bonLivraison.getNumero().trim().isEmpty()) {
                bonLivraison.setNumero(generateNumero());
            }
            
            BonLivraison savedBon;
            if (bonLivraison.getId() == null) {
                em.persist(bonLivraison);
                savedBon = bonLivraison;
            } else {
                savedBon = em.merge(bonLivraison);
            }
            
            transaction.commit();
            return savedBon;
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du bon de livraison: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(BonLivraison bonLivraison) {
        if (bonLivraison == null || bonLivraison.getId() == null) {
            return;
        }
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            BonLivraison managedBon = em.find(BonLivraison.class, bonLivraison.getId());
            if (managedBon != null) {
                em.remove(managedBon);
            }
            
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du bon de livraison: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void deleteById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            BonLivraison bonLivraison = em.find(BonLivraison.class, id);
            if (bonLivraison != null) {
                em.remove(bonLivraison);
            }
            
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du bon de livraison: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public boolean existsByNumero(String numero) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT COUNT(bl) FROM BonLivraison bl WHERE bl.numero = :numero");
            query.setParameter("numero", numero);
            Long count = (Long) query.getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
    
    private String generateNumero() {
        String prefix = "BL";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Query query = em.createQuery("SELECT COUNT(bl) FROM BonLivraison bl WHERE bl.numero LIKE :pattern");
            query.setParameter("pattern", prefix + datePart + "%");
            Long count = (Long) query.getSingleResult();
            
            return String.format("%s%s%03d", prefix, datePart, count + 1);
        } finally {
            em.close();
        }
    }
    
    public LigneLivraison addLigneToBon(BonLivraison bonLivraison, Article article, BigDecimal quantiteDemandee, BigDecimal quantiteLivree) {
        LigneLivraison ligne = new LigneLivraison();
        ligne.setArticle(article);
        ligne.setQuantiteDemandee(quantiteDemandee);
        ligne.setQuantiteLivree(quantiteLivree != null ? quantiteLivree : quantiteDemandee);
        
        bonLivraison.addLigne(ligne);
        return ligne;
    }
    
    public void removeLigneFromBon(BonLivraison bonLivraison, LigneLivraison ligne) {
        bonLivraison.removeLigne(ligne);
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
            Query query = em.createQuery("SELECT c FROM Client c WHERE c.type = 'CLIENT' OR c.type = 'CLIENT_FOURNISSEUR' ORDER BY c.nom, c.prenom", Client.class);
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
            Query query = em.createQuery("SELECT d FROM Devis d WHERE d.client.id = :clientId AND d.statut = 'ACCEPTE' ORDER BY d.dateCreation DESC", Devis.class);
            query.setParameter("clientId", client.getId());
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public BonLivraison createFromDevis(Devis devis) {
        if (devis == null) {
            return null;
        }
        
        BonLivraison bonLivraison = new BonLivraison();
        bonLivraison.populateFromDevis(devis);
        
        return bonLivraison;
    }
    
    public BonLivraison duplicateBonLivraison(BonLivraison originalBon) {
        if (originalBon == null) {
            return null;
        }
        
        BonLivraison newBon = new BonLivraison();
        newBon.setClient(originalBon.getClient());
        newBon.setAdresseLivraison(originalBon.getAdresseLivraison());
        newBon.setObservations(originalBon.getObservations());
        newBon.setTransporteur(originalBon.getTransporteur());
        newBon.setModeLivraison(originalBon.getModeLivraison());
        
        // Copy lines
        for (LigneLivraison originalLigne : originalBon.getLignes()) {
            LigneLivraison newLigne = new LigneLivraison();
            newLigne.setArticle(originalLigne.getArticle());
            newLigne.setQuantiteDemandee(originalLigne.getQuantiteDemandee());
            newLigne.setQuantiteLivree(originalLigne.getQuantiteLivree());
            newLigne.setDescription(originalLigne.getDescription());
            newLigne.setEmplacement(originalLigne.getEmplacement());
            
            newBon.addLigne(newLigne);
        }
        
        return newBon;
    }
}
